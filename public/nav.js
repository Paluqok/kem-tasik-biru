document.addEventListener('DOMContentLoaded', (event) => {
    const menuToggle = document.getElementById('menuToggle');
    const sideNav = document.getElementById('sideNav');

    menuToggle.addEventListener('click', () => {
        sideNav.classList.toggle('open');
    });
});
