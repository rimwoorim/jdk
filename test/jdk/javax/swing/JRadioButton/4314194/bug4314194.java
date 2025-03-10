/*
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * @test
 * @key headful
 * @bug 4314194 8075916
 * @summary  Verifies disabled color for JCheckbox and JRadiobutton is honored in all L&F
 * @run main bug4314194
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class bug4314194 {
    private static JFrame frame;
    private static JRadioButton radioButton;
    private static JCheckBox checkBox;
    private static Point point;
    private static Rectangle rect;
    private static Robot robot;
    private static final Color radioButtonColor = Color.RED;
    private static final Color checkboxColor = Color.GREEN;
    private static final int tolerance = 20;

    private static boolean checkComponent(Component comp, Color c) throws Exception {
        int correctColoredPixels = 0;
        int totalPixels = 0;

        SwingUtilities.invokeAndWait(() -> {
            point = comp.getLocationOnScreen();
            rect = comp.getBounds();
        });

        int y = point.y + rect.height / 2;
        for (int x = point.x; x < point.x + rect.width; x++) {
            Color color = robot.getPixelColor(x, y);
            robot.waitForIdle();

            if (color.equals(c)) {
                correctColoredPixels++;
            }
            totalPixels++;
        }

        return ((double)correctColoredPixels/totalPixels*100) >= tolerance;
    }

    private static void setLookAndFeel(UIManager.LookAndFeelInfo laf) {
        try {
            UIManager.setLookAndFeel(laf.getClassName());
        } catch (UnsupportedLookAndFeelException ignored) {
            System.out.println("Unsupported L&F: " + laf.getClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createUI() {
        UIManager.getDefaults().put("CheckBox.disabledText", checkboxColor);
        UIManager.getDefaults().put("RadioButton.disabledText", radioButtonColor);

        checkBox = new JCheckBox("WWWWW");
        radioButton = new JRadioButton("WWWWW");
        checkBox.setFont(checkBox.getFont().deriveFont(50.0f));
        radioButton.setFont(radioButton.getFont().deriveFont(50.0f));
        checkBox.setEnabled(false);
        radioButton.setEnabled(false);

        frame = new JFrame("bug4314194");
        frame.getContentPane().add(radioButton, BorderLayout.SOUTH);
        frame.getContentPane().add(checkBox, BorderLayout.NORTH);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    public static void main(String[] args) throws Exception {
        robot = new Robot();
        robot.setAutoDelay(100);

        for (UIManager.LookAndFeelInfo laf :
                 UIManager.getInstalledLookAndFeels()) {
            if (laf.getClassName().contains("Motif")) {
                System.out.println("Skipping Motif L&F as it is deprecated");
                continue;
            }
            System.out.println("Testing L&F: " + laf.getClassName());
            SwingUtilities.invokeAndWait(() -> setLookAndFeel(laf));
            try {
                SwingUtilities.invokeAndWait(() -> createUI());
                robot.waitForIdle();
                robot.delay(1000);

                if (!checkComponent(checkBox, checkboxColor)) {
                    throw new RuntimeException("Correct color not set for Checkbox");
                }

                if (!checkComponent(radioButton, radioButtonColor)) {
                    throw new RuntimeException("Correct color not set for RadioButton");
                }
            } finally {
                if (frame != null) {
                    SwingUtilities.invokeAndWait(() -> frame.dispose());
                }
            }
        }
    }
}

