package com.github.friendlyanon.twopowtwelve.gui;

import javax.swing.*;

public class SetupForm {
    public JPanel panel;
    public JButton OKButton;
    public JComboBox<String> comboBoxGrid;
    public JRadioButton freePlayRadioButton;
    public JRadioButton exponentReachedRadioButton;
    public JSpinner spinnerExponent;

    SetupForm() {
        spinnerExponent.setModel(new SpinnerNumberModel(12, 4, 99, 1));
        var options = new String[7];
        for (var i = 0; i < 7; ++i) {
            options[i] = String.valueOf(i + 4) + '×' + (i + 4);
        }
        comboBoxGrid.setModel(new DefaultComboBoxModel<>(options));
        exponentReachedRadioButton.setSelected(true);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
