package ru.asb.program.bridge.gui;

import javax.swing.*;

/**
 * Класс выпадающего меню при нажатии правой кнопки
 */
class ContextMenu extends JPopupMenu {
    private JMenuItem removeMenuRow;
    private JMenuItem addMenuRow;

    ContextMenu(boolean remove){
        if (remove) {
            removeMenuRow = new JMenuItem("Удалить строку");
            add(removeMenuRow);
        } else {
            addMenuRow = new JMenuItem("Добавить строку");
            add(addMenuRow);
        }
    }

    JMenuItem getRemoveMenuRow() {
        return this.removeMenuRow;
    }

    JMenuItem getAddMenuRow() {
        return this.addMenuRow;
    }
}
