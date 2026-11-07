"""Comprueba autenticación, autorización y CSRF contra el perfil seguro real."""
import argparse,uuid
from pathlib import Path
from playwright.sync_api import sync_playwright

def check(base):
    with sync_playwright() as pw:
        browser=pw.chromium.launch()
        for user in ('lector','gestor'):
            context=browser.new_context(viewport={'width':390,'height':844},color_scheme='dark');p=context.new_page()
            p.goto(base+'/libros',wait_until='networkidle');assert '/login' in p.url
            p.locator('[name=username]').fill(user);p.locator('[name=password]').fill('incorrecta');p.get_by_role('button',name='Sign in').click();p.wait_for_load_state('networkidle');assert 'error' in p.url
            p.locator('[name=username]').fill(user);p.locator('[name=password]').fill(user+'_local');p.get_by_role('button',name='Sign in').click();p.wait_for_load_state('networkidle');assert '/login' not in p.url
            p.goto(base+'/editoriales',wait_until='networkidle');csrf=p.locator('input[name=_csrf]').first.input_value()
            payload={'nombre':'QA-SEG-'+uuid.uuid4().hex[:8],'_csrf':csrf}
            assert context.request.post(base+'/editoriales/guardar',form={'nombre':payload['nombre']}).status==403
            response=context.request.post(base+'/editoriales/guardar',form=payload)
            if user=='lector':
                assert response.status==403
                assert p.get_by_role('button',name='Guardar editorial',exact=True).count()==0
            else:
                assert response.status==200 and payload['nombre'] in response.text()
                p.reload(wait_until='networkidle');row=p.locator('tbody tr').filter(has_text=payload['nombre']);row.get_by_role('button',name='Eliminar',exact=True).click();p.locator('#confirmar-operacion').click();p.wait_for_load_state('networkidle');assert p.locator('tbody tr').filter(has_text=payload['nombre']).count()==0
            for path in ('/libros','/editoriales','/operaciones','/pedidos'):
                r=p.goto(base+path,wait_until='networkidle');assert r.ok
                if user=='lector' and path=='/operaciones':
                    assert p.get_by_role('button',name='Calcular presupuesto',exact=True).count()==0
                    assert 'rol gestor' in p.locator('.solo-lectura').inner_text()
                assert p.evaluate('document.documentElement.scrollWidth<=innerWidth+2'),(user,path,'overflow')
                Path('qa-captures').mkdir(exist_ok=True);p.screenshot(path=f'qa-captures/spring-seguro-{user}-{path[1:]}-390-dark.png',full_page=True)
            p.get_by_role('button',name='Salir',exact=True).click();p.wait_for_load_state('networkidle');assert '/login' in p.url
            context.close()
        browser.close()
    print('PASS Spring seguro: loginincorrecto/correcto2roles, LECTOR403, GESTORCRUD, CSRF403/válido y logoutPOST; móviloscuro4p')
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--base',required=True);args=a.parse_args();check(args.base.rstrip('/'))
