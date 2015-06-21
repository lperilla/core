package org.lperilla.framework.core.utils;

import java.awt.Component;
import java.awt.Dimension;

public final class ComponentUtil {

	/**
	 * Use this static method if you want to center a component over another
	 * component.
	 *
	 * @param parent
	 *            the component you want to use to place it on
	 * @param toBeCentered
	 *            the component you want to center
	 */
	public static void centerComponentInComponent(Component parent, Component toBeCentered) {
		toBeCentered.setLocation(parent.getX() + (parent.getWidth() - toBeCentered.getWidth()) / 2, parent.getY() + (parent.getHeight() - toBeCentered.getHeight()) / 2);

		toBeCentered.validate();
		toBeCentered.repaint();
	}

	/**
	 * Use this static method if you want to center a component in Window.
	 *
	 * @param component
	 *            the component you want to center in window
	 */
	public static void centerComponentInWindow(Component component) {
		Dimension dimension = component.getToolkit().getScreenSize();

		component.setLocation((int) ((dimension.getWidth() - component.getWidth()) / 2), (int) ((dimension.getHeight() - component.getHeight()) / 2));
		component.validate();
		component.repaint();
	}

	/**
	 * Use this static method if you want to center and set its position
	 * compared to the size of the current users screen size. Valid percent is
	 * between +-(0-100) minus is treated as plus, bigger than 100 is always set
	 * to 100.
	 *
	 * @param component
	 *            the component you want to center and set size on
	 * @param percentOfScreen
	 *            the percent of the current screensize you want the component
	 *            to be
	 */
	public static void centerComponentInWindow(Component component, int percentOfScreen) {
		if (percentOfScreen < 0) {
			centerComponentInWindow(component, -percentOfScreen);
			return;
		}
		if (percentOfScreen > 100) {
			centerComponentInWindow(component, 100);
			return;
		}
		double percent = percentOfScreen / 100.d;
		Dimension dimension = component.getToolkit().getScreenSize();
		component.setSize((int) (dimension.getWidth() * percent), (int) (dimension.getHeight() * percent));
		centerComponentInWindow(component);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ComponentUtil() {
	}

}
