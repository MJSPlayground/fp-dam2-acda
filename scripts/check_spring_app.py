"""QA de la interfaz Spring contra una aplicación y MySQL reales de prueba.
Crea datos con prefijo QA; los pedidos se conservan como instantáneas para comprobarlos.
"""
from __future__ import annotations
import argparse
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import uuid
from urllib.request import Request,urlopen
from urllib.parse import urlencode
from urllib.error import HTTPError
from playwright.sync_api import sync_playwright


def check(base, label):
    token="QA-"+uuid.uuid4().hex[:10]
    title=token+', "edición"\nsegunda línea'
    isbn="978"+str(uuid.uuid4().int%10**10).zfill(10)
    captures=Path('qa-captures');captures.mkdir(exist_ok=True)
    with sync_playwright() as pw:
        browser=pw.chromium.launch()
        context=browser.new_context(viewport={"width":1366,"height":900})
        p=context.new_page();errors=[];p.on('pageerror',lambda e: errors.append(str(e)))
        def go(path):
            r=p.goto(base+path,wait_until='networkidle');assert r and r.ok,(path,r.status if r else None)
        def confirmation(button, accept=True):
            button.click();d=p.locator('#confirmacion');assert d.is_visible()
            assert d.locator('[value=cancelar]').evaluate('e=>e===document.activeElement')
            d.locator('#confirmar-operacion' if accept else '[value=cancelar]').click()
            p.wait_for_load_state('networkidle')
        go('/editoriales');p.locator('[name=nombre]').fill(token+' editorial');p.get_by_role('button',name='Guardar editorial',exact=True).click();p.wait_for_load_state('networkidle')
        row=p.locator('tbody tr').filter(has_text=token);assert row.count()==1
        editorial_id=row.locator('a').get_attribute('href').split('/')[2]
        go('/libros/nuevo');p.locator('[name=isbn]').fill(isbn);p.locator('[name=titulo]').fill(title);p.locator('[name=precio]').fill('25.00');p.locator('[name=stock]').fill('10');p.locator('[name=editorialId]').select_option(editorial_id);p.get_by_role('button',name='Guardar libro',exact=True).click();p.wait_for_load_state('networkidle')
        row=p.locator('tbody tr').filter(has_text=token);assert row.count()==1
        edit_path=row.get_by_role('link',name='Editar',exact=True).get_attribute('href');book_id=edit_path.split('/')[2]
        go(edit_path);assert p.locator('[name=titulo]').input_value()==title
        save=p.get_by_role('button',name='Guardar libro',exact=True).bounding_box();cancel=p.get_by_role('link',name='Cancelar',exact=True).bounding_box();assert save['height']>=44 and cancel['height']>=44;assert cancel['x']>=save['x']+save['width']+7
        p.locator('[name=precio]').fill('26.00');p.get_by_role('link',name='Cancelar',exact=True).click();go(edit_path);assert p.locator('[name=precio]').input_value()=='25.00'
        p.locator('[name=precio]').fill('26.00');p.get_by_role('button',name='Guardar libro',exact=True).click();p.wait_for_load_state('networkidle');go(edit_path);assert p.locator('[name=titulo]').input_value()==title
        save=p.get_by_role('button',name='Guardar libro',exact=True).bounding_box();cancel=p.get_by_role('link',name='Cancelar',exact=True).bounding_box();assert save['height']>=44 and cancel['height']>=44;assert cancel['x']>=save['x']+save['width']+7;assert p.locator('[name=precio]').input_value()=='26.00'
        # Bypass browser validation to exercise backend validation and preserve the form.
        data=p.locator('form.editor').evaluate('f=>Object.fromEntries(new FormData(f))');data['stock']='3.5'
        bad=context.request.post(base+'/libros/guardar',form=data);assert bad.status==200 and 'typeMismatch' not in bad.text();assert 'error' in bad.text()
        go(edit_path);assert p.locator('[name=stock]').input_value()=='10';version=p.locator('[name=version]').input_value()
        # CSRF enforcement also applies to the default local profile.
        missing=context.request.post(base+'/libros/guardar',form={k:v for k,v in data.items() if k!='_csrf'});assert missing.status==403
        # Stale forms must not overwrite a newer version.
        fresh=p.locator('form.editor').evaluate('f=>Object.fromEntries(new FormData(f))');fresh['precio']='27.00';assert context.request.post(base+'/libros/guardar',form=fresh).ok
        stale=context.request.post(base+'/libros/guardar',form=fresh);assert stale.status==200 and ('cambió' in stale.text() or 'versión' in stale.text())
        go(edit_path);assert p.locator('[name=precio]').input_value()=='27.00'
        if label=='final':
            go('/operaciones');p.locator('#libro-reponer').select_option(book_id);p.locator('#reposicion [name=unidades]').fill('2');p.locator('#reposicion button').click();p.wait_for_load_state('networkidle');go(edit_path);assert p.locator('[name=stock]').input_value()=='12'
            go('/operaciones');p.locator('#clave').fill(token+'-pedido');line=p.locator('#seleccion tbody tr').filter(has_text=token);line.locator('input[type=number]').fill('4')
            p.get_by_role('button',name='Calcular presupuesto',exact=True).click();p.wait_for_load_state('networkidle');assert '102.60' in p.locator('section.aviso').inner_text();go(edit_path);assert p.locator('[name=stock]').input_value()=='12'
            go('/pedidos');assert p.locator('article').filter(has_text=token+'-pedido').count()==0
            go('/operaciones');p.locator('#clave').fill(token+'-pedido');p.locator('#seleccion tbody tr').filter(has_text=token).locator('input[type=number]').fill('4')
            confirmation(p.get_by_role('button',name='Confirmar pedido',exact=True),False);assert p.url.endswith('/operaciones')
            confirmation(p.get_by_role('button',name='Confirmar pedido',exact=True));assert p.url.endswith('/pedidos');assert p.locator('article').filter(has_text=token+'-pedido').count()==1
            go(edit_path);assert p.locator('[name=stock]').input_value()=='8'
            # The order snapshot must survive later catalog edits.
            p.locator('[name=titulo]').fill(token+' posterior');p.locator('[name=precio]').fill('99.00');p.get_by_role('button',name='Guardar libro',exact=True).click();go('/pedidos');snapshot=p.locator('article').filter(has_text=token+'-pedido').inner_text();assert '102.60' in snapshot and '27.00' in snapshot and 'posterior' not in snapshot
            # Two concurrent confirmations with the same key may save only one order/decrement.
            go('/operaciones');p.locator('#clave').fill(token+'-concurrente');p.locator('#seleccion tbody tr').filter(has_text=token).locator('input[type=number]').fill('1');payload=p.locator('#seleccion').evaluate('f=>Object.fromEntries(new FormData(f))')
            cookies='; '.join(c['name']+'='+c['value'] for c in context.cookies())
            def submit(_):
                req=Request(base+'/operaciones/confirmar',data=urlencode(payload).encode(),headers={'Cookie':cookies,'Content-Type':'application/x-www-form-urlencoded'})
                try:r=urlopen(req,timeout=20)
                except HTTPError as e:r=e
                return r.status,r.read().decode()
            with ThreadPoolExecutor(max_workers=2) as pool:responses=list(pool.map(submit,range(2)))
            assert all(status==200 for status,_ in responses),responses
            go('/pedidos');assert p.locator('article').filter(has_text=token+'-concurrente').count()==1
            go(edit_path);assert p.locator('[name=stock]').input_value()=='7'
        for width in (1366,390):
            p.set_viewport_size({'width':width,'height':900})
            for scheme in ('light','dark'):
                p.emulate_media(color_scheme=scheme)
                for path in ('/libros','/editoriales',edit_path)+ (('/operaciones','/pedidos') if label=='final' else ()):
                    go(path);assert p.evaluate('document.documentElement.scrollWidth<=innerWidth+2'),(path,width,scheme)
                    p.screenshot(path=str(captures/f'spring-{label}-{path.strip("/").replace("/","-")}-{width}-{scheme}.png'),full_page=True)
        go('/libros');row=p.locator('tbody tr').filter(has_text=token);confirmation(row.get_by_role('button',name='Eliminar',exact=True),False);assert row.count()==1;confirmation(row.get_by_role('button',name='Eliminar',exact=True));assert p.locator('tbody tr').filter(has_text=token).count()==0
        go('/editoriales');row=p.locator('tbody tr').filter(has_text=token);confirmation(row.get_by_role('button',name='Eliminar',exact=True));assert p.locator('tbody tr').filter(has_text=token).count()==0
        assert not errors,errors
        context.close();browser.close()
    print(f'PASS Spring {label}: CRUD/validación/CSRF/versiones/conservación y UI4combinaciones; finaloperaciones si corresponde; datos {token}')

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--base',required=True);parser.add_argument('--label',choices=('guiado','final','final-inicial'),required=True);a=parser.parse_args();check(a.base.rstrip('/'),a.label)
