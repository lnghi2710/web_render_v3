(function () {
    'use strict';

    // ---------- Thanh điều hướng trên màn hình nhỏ ----------
    var sidebar = document.getElementById('sidebar');
    var scrim = document.getElementById('scrim');
    function dong() { if (sidebar) sidebar.classList.remove('is-open'); if (scrim) scrim.classList.remove('is-open'); }
    document.querySelectorAll('[data-toggle-sidebar]').forEach(function (b) {
        b.addEventListener('click', function () {
            sidebar.classList.toggle('is-open');
            scrim.classList.toggle('is-open', sidebar.classList.contains('is-open'));
        });
    });
    if (scrim) scrim.addEventListener('click', dong);
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') dong(); });
    var active = document.querySelector('.sidebar-nav .side-link.is-active');
    if (active && active.scrollIntoView) active.scrollIntoView({ block: 'nearest' });

    // ---------- Đóng thông báo ----------
    document.querySelectorAll('.notice .close').forEach(function (b) {
        b.addEventListener('click', function () { b.closest('.notice').remove(); });
    });

    // ---------- Chọn đợt: tự gửi khi đổi ----------
    document.querySelectorAll('[data-autosubmit]').forEach(function (s) {
        s.addEventListener('change', function () { if (s.form) s.form.submit(); });
    });

    // ---------- Hộp thoại xác nhận thay cho confirm() của trình duyệt ----------
    var modalEl = document.getElementById('confirmModal');
    var okBtn = document.getElementById('confirmOk');
    var bodyEl = document.getElementById('confirmBody');
    var modal = modalEl && window.bootstrap ? new window.bootstrap.Modal(modalEl) : null;
    var pending = null;
    document.addEventListener('submit', function (e) {
        var f = e.target;
        var msg = f && f.getAttribute ? f.getAttribute('data-confirm') : null;
        if (!msg) return;
        if (!modal) { if (!window.confirm(msg)) e.preventDefault(); return; }
        e.preventDefault();
        pending = f;
        var nguyHiem = f.getAttribute('data-confirm-style') === 'danger';
        bodyEl.textContent = msg;
        okBtn.className = 'btn ' + (nguyHiem ? 'btn-danger' : 'btn-primary');
        okBtn.textContent = nguyHiem ? 'Xác nhận' : 'Đồng ý';
        modal.show();
    });
    if (modalEl) {
        modalEl.addEventListener('shown.bs.modal', function () { okBtn.focus(); });
        okBtn.addEventListener('click', function () {
            if (!pending) return;
            var f = pending;
            pending = null;
            modal.hide();
            f.submit(); // submit() không kích hoạt lại sự kiện submit nên không hỏi lần hai
        });
    }

    // ---------- Bảng: sắp xếp theo cột và phân trang (table[data-grid]) ----------
    function khoa(s) {
        var m = /^(\d{2})\/(\d{2})\/(\d{4})(?:\s+(\d{2}):(\d{2})(?::(\d{2}))?)?$/.exec(s);
        if (m) return { n: new Date(+m[3], +m[2] - 1, +m[1], +(m[4] || 0), +(m[5] || 0), +(m[6] || 0)).getTime() };
        var so = s.replace(',', '.');
        if (/^-?\d+(\.\d+)?$/.test(so)) return { n: parseFloat(so) };
        return { s: s };
    }
    function soSanh(a, b) {
        if (a === '' && b === '') return 0;
        if (a === '') return 1;   // ô trống luôn xuống cuối
        if (b === '') return -1;
        var x = khoa(a), y = khoa(b);
        if (x.n !== undefined && y.n !== undefined) return x.n - y.n;
        return a.localeCompare(b, 'vi', { numeric: true, sensitivity: 'base' });
    }

    function khoiTaoBang(table) {
        var tbody = table.tBodies[0];
        if (!tbody) return;
        var rows = Array.prototype.slice.call(tbody.rows);
        var size = parseInt(table.getAttribute('data-page-size') || '10', 10);
        var st = { col: -1, dir: 1, page: 1 };
        rows.forEach(function (r) { r._an = {}; });
        var pager = document.createElement('div');
        pager.className = 'grid-pager';
        var wrap = table.closest('.table-wrap') || table;
        wrap.parentNode.insertBefore(pager, wrap.nextSibling);
        var ths = table.tHead && table.tHead.rows[0] ? Array.prototype.slice.call(table.tHead.rows[0].cells) : [];

        ths.forEach(function (th, i) {
            if (!th.textContent.trim() || th.hasAttribute('data-nosort')) return;
            th.classList.add('sortable');
            th.setAttribute('tabindex', '0');
            th.setAttribute('aria-sort', 'none');
            function chon() {
                if (st.col === i) st.dir = -st.dir; else { st.col = i; st.dir = 1; }
                st.page = 1;
                ve();
            }
            th.addEventListener('click', chon);
            th.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); chon(); }
            });
        });

        function giaTri(r, i) {
            var c = r.cells[i];
            if (!c) return '';
            var d = c.getAttribute('data-sort');
            return (d !== null ? d : c.textContent).replace(/\s+/g, ' ').trim();
        }
        function an(r) { for (var k in r._an) { if (r._an[k]) return true; } return false; }

        function nut(nhan, trang, tat, hienTai, aria) {
            var b = document.createElement('button');
            b.type = 'button';
            b.className = 'btn btn-sm ' + (hienTai ? 'btn-primary' : 'btn-outline-secondary');
            b.textContent = nhan;
            if (aria) b.setAttribute('aria-label', aria);
            if (hienTai) b.setAttribute('aria-current', 'page');
            b.disabled = !!tat;
            b.addEventListener('click', function () { st.page = trang; ve(); });
            return b;
        }

        function ve() {
            var thuTu = rows.slice();
            if (st.col >= 0) {
                var idx = rows.indexOf.bind(rows);
                thuTu.sort(function (a, b) {
                    var c = soSanh(giaTri(a, st.col), giaTri(b, st.col)) * st.dir;
                    return c !== 0 ? c : idx(a) - idx(b); // ổn định
                });
                thuTu.forEach(function (r) { tbody.appendChild(r); });
            }
            ths.forEach(function (th, i) {
                if (!th.classList.contains('sortable')) return;
                th.setAttribute('aria-sort', st.col === i ? (st.dir === 1 ? 'ascending' : 'descending') : 'none');
            });
            var hien = thuTu.filter(function (r) { return !an(r); });
            var tong = hien.length;
            var soTrang = Math.max(1, Math.ceil(tong / size));
            if (st.page > soTrang) st.page = soTrang;
            var tu = (st.page - 1) * size;
            thuTu.forEach(function (r) { r.style.display = 'none'; });
            hien.slice(tu, tu + size).forEach(function (r) { r.style.display = ''; });

            pager.textContent = '';
            if (tong <= size) { pager.style.display = 'none'; return; }
            pager.style.display = '';
            var info = document.createElement('span');
            info.className = 'sub-text';
            info.textContent = 'Hiển thị ' + (tu + 1) + '–' + Math.min(tu + size, tong) + ' trên ' + tong;
            var ds = document.createElement('div');
            ds.className = 'pager-btns';
            ds.appendChild(nut('Trước', st.page - 1, st.page === 1, false, 'Trang trước'));
            var dau = Math.max(1, st.page - 2), cuoi = Math.min(soTrang, dau + 4);
            dau = Math.max(1, cuoi - 4);
            for (var p = dau; p <= cuoi; p++) ds.appendChild(nut(String(p), p, false, p === st.page, 'Trang ' + p));
            ds.appendChild(nut('Sau', st.page + 1, st.page === soTrang, false, 'Trang sau'));
            pager.appendChild(info);
            pager.appendChild(ds);
        }

        table._grid = {
            // nguon: tên bộ lọc; fn(row) trả về true nếu hàng bị ẩn
            loc: function (nguon, fn) {
                rows.forEach(function (r) { r._an[nguon] = fn(r); });
                st.page = 1;
                ve();
                var khong = rows.length > 0 && rows.every(an);
                var panel = table.closest('.panel');
                var tb = panel ? panel.querySelector('[data-no-match]') : null;
                if (tb) tb.classList.toggle('d-none', !khong);
            }
        };
        ve();
    }
    document.querySelectorAll('table[data-grid]').forEach(khoiTaoBang);

    // ---------- Lọc nhanh: <input data-filter="#idBang"> ----------
    document.querySelectorAll('[data-filter]').forEach(function (input) {
        var table = document.querySelector(input.getAttribute('data-filter'));
        input.addEventListener('input', function () {
            var q = input.value.trim().toLowerCase();
            if (table && table._grid) {
                table._grid.loc('q', function (tr) { return q !== '' && tr.textContent.toLowerCase().indexOf(q) === -1; });
            } else if (table) {
                table.querySelectorAll('tbody tr[data-row]').forEach(function (tr) {
                    tr.style.display = tr.textContent.toLowerCase().indexOf(q) === -1 ? 'none' : '';
                });
            }
        });
        input.addEventListener('keydown', function (e) { if (e.key === 'Enter') e.preventDefault(); });
    });

    // ---------- Bộ lọc nhiều tiêu chí (kho đề tài): data-filter-group="#idBang" ----------
    document.querySelectorAll('[data-filter-group]').forEach(function (nhom) {
        var table = document.querySelector(nhom.getAttribute('data-filter-group'));
        if (!table || !table._grid) return;
        function ap() {
            var q = (nhom.querySelector('[data-f="q"]') || { value: '' }).value.trim().toLowerCase();
            var bm = (nhom.querySelector('[data-f="bomon"]') || { value: '' }).value;
            var gv = (nhom.querySelector('[data-f="gv"]') || { value: '' }).value;
            var tr = nhom.querySelector('[data-f="trong"]');
            var chiTrong = tr && tr.checked;
            table._grid.loc('nhom', function (r) {
                if (q && r.textContent.toLowerCase().indexOf(q) === -1) return true;
                if (bm && r.getAttribute('data-bomon') !== bm) return true;
                if (gv && (r.getAttribute('data-gv') || '').split(', ').indexOf(gv) === -1) return true;
                if (chiTrong && r.getAttribute('data-trong') !== 'true') return true;
                return false;
            });
        }
        nhom.querySelectorAll('[data-f]').forEach(function (c) {
            c.addEventListener('input', ap);
            c.addEventListener('change', ap);
        });
    });
})();
