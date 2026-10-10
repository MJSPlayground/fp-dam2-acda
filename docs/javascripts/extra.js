/* Checklists privadas de navegador: no hay backend ni transmisión de datos. */
(() => {
  function initTasks() {
    const boxes = [...document.querySelectorAll('.mjs-task input[type="checkbox"]')];
    if (!boxes.length) return;
    const pathname = window.location.pathname;
    const update = () => {
      const done = boxes.filter((box) => box.checked).length;
      const counter = document.querySelector('.mjs-progress-count');
      const fill = document.querySelector('.mjs-progress-fill');
      if (counter) counter.textContent = done + ' de ' + boxes.length + ' pasos';
      if (fill) fill.style.width = (100 * done / boxes.length) + '%';
    };
    for (const box of boxes) {
      if (box.dataset.mjsInit === '1') continue;
      box.dataset.mjsInit = '1';
      const key = 'mjs-demo-task:' + pathname + ':' + box.id;
      try { box.checked = localStorage.getItem(key) === 'true'; } catch (_) { /* almacenamiento bloqueado */ }
      box.addEventListener('change', () => {
        try { localStorage.setItem(key, String(box.checked)); } catch (_) { /* navegación continúa */ }
        update();
      });
    }
    update();
  }
  if (typeof document$ !== 'undefined' && document$.subscribe) {
    document$.subscribe(initTasks);
  } else if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTasks);
  } else {
    initTasks();
  }
})();


/* Acceso global al temario, sin páginas ni botones de descarga redundantes.
   Los PDFs corresponden a la última compilación de la web publicada. */
(() => {
  const icon = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 3v12m-4-4 4 4 4-4"/><path d="M4 17v3h16v-3"/></svg>';
  let menu = null;
  let button = null;

  const baseUrl = () => {
    const homeLink = document.querySelector('a.md-header__button.md-logo');
    if (!homeLink) return new URL('/', window.location.href);
    return new URL(homeLink.href);
  };
  const currentSection = () => {
    const base = baseUrl();
    const path = decodeURIComponent(window.location.pathname);
    const root = decodeURIComponent(base.pathname);
    if (!path.startsWith(root)) return null;
    const slug = path.slice(root.length).split('/')[0];
    if (!/^[a-z0-9][a-z0-9_-]*$/i.test(slug)) return null;
    const links = [...document.querySelectorAll('.md-tabs__link')];
    const matched = links.find(link => {
      const url = new URL(link.href);
      const prefix = root + slug + '/';
      return url.origin === window.location.origin &&
             (url.pathname === prefix || url.pathname.startsWith(prefix));
    });
    if (!matched) return null;
    return { id: slug, label: matched.textContent.trim() || slug };
  };
  const pdfUrl = file => new URL('descargas/' + file, baseUrl()).href;

  const close = () => {
    if (!menu) return;
    menu.hidden = true;
    button?.setAttribute('aria-expanded', 'false');
  };

  const buildMenu = () => {
    if (!menu) return;
    menu.replaceChildren();
    const unit = currentSection();
    const links = unit
      ? [{ label: 'Descargar ' + unit.label, info: 'Solo esta sección', file: unit.id + '.pdf' },
         { label: 'Descargar documentación completa', info: 'Todas las secciones', file: 'documentacion-completa.pdf' }]
      : [{ label: 'Descargar documentación completa', info: 'Todas las secciones', file: 'documentacion-completa.pdf' }];

    for (const item of links) {
      const a = document.createElement('a');
      a.className = 'mjs-pdf-menu__item';
      a.href = pdfUrl(item.file);
      a.setAttribute('download', item.file);
      a.setAttribute('role', 'menuitem');
      const title = document.createElement('span');
      title.className = 'mjs-pdf-menu__title';
      title.textContent = item.label;
      const subtitle = document.createElement('small');
      subtitle.textContent = item.info + ' · PDF';
      a.append(title, subtitle);
      a.addEventListener('click', close);
      menu.append(a);
    }
  };

  const toggle = () => {
    if (!menu || !button) return;
    if (!menu.hidden) { close(); return; }
    buildMenu();
    const rect = button.getBoundingClientRect();
    menu.style.top = Math.round(rect.bottom + 8) + 'px';
    menu.style.right = Math.max(8, Math.round(window.innerWidth - rect.right - 8)) + 'px';
    menu.hidden = false;
    button.setAttribute('aria-expanded', 'true');
    menu.querySelector('a')?.focus();
  };

  function initPdfMenu() {
    const header = document.querySelector('.md-header__inner');
    if (!header) return;
    // Permite volver a la portada tanto desde el logo como desde el título.
    const siteTitle = header.querySelector('.md-header__title .md-header__topic:first-child .md-ellipsis');
    if (siteTitle && !siteTitle.querySelector('a.mjs-home-link')) {
      const link = document.createElement('a');
      link.className = 'mjs-home-link';
      link.href = baseUrl().href;
      link.title = 'Volver al inicio';
      link.textContent = siteTitle.textContent.trim();
      siteTitle.replaceChildren(link);
    }
    if (!button || !button.isConnected) {
      const action = document.createElement('div');
      action.className = 'mjs-pdf-action';
      button = document.createElement('button');
      button.type = 'button';
      button.className = 'md-header__button md-icon mjs-pdf-trigger';
      button.setAttribute('aria-label', 'Descargar temario en PDF');
      button.setAttribute('title', 'Descargar PDF');
      button.setAttribute('aria-haspopup', 'true');
      button.setAttribute('aria-expanded', 'false');
      button.innerHTML = icon;
      action.append(button);
      const search = header.querySelector('label[for="__search"]');
      if (search) header.insertBefore(action, search);
      else header.append(action);
      button.addEventListener('click', toggle);
    }
    if (!menu || !menu.isConnected) {
      menu = document.createElement('div');
      menu.className = 'mjs-pdf-menu';
      menu.setAttribute('role', 'menu');
      menu.setAttribute('aria-label', 'Opciones de descarga PDF');
      menu.hidden = true;
      document.body.append(menu);
    }
    close();
  }
  document.addEventListener('pointerdown', event => {
    if (menu && !menu.hidden && !menu.contains(event.target) && !button?.contains(event.target)) close();
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && menu && !menu.hidden) {
      close(); button?.focus();
    }
  });
  window.addEventListener('resize', close);
  window.addEventListener('scroll', close, { passive: true });
  if (typeof document$ !== 'undefined' && document$.subscribe) document$.subscribe(initPdfMenu);
  else if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initPdfMenu);
  else initPdfMenu();
})();
