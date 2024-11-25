package com.gtnewhorizons.CTF.utils.visualizer.api.src.main.java.com.github.skjolber.packing.visualizer.api.packaging;

import com.gtnewhorizons.CTF.utils.visualizer.api.src.main.java.com.github.skjolber.packing.visualizer.api.VisualizerPlugin;

import java.util.ArrayList;
import java.util.List;


public class AbstractVisualizer {

	private int step;

	private List<VisualizerPlugin> plugins = new ArrayList<>();

	public void setStep(int step) {
		this.step = step;
	}

	public int getStep() {
		return step;
	}

	public void setPlugins(List<VisualizerPlugin> plugins) {
		this.plugins = plugins;
	}

	public List<VisualizerPlugin> getPlugins() {
		return plugins;
	}
}
