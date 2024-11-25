package com.gtnewhorizons.CTF.utils.visualizer.packaging.src.main.java.com.github.skjolber.packing.visualizer.packaging;

import com.gtnewhorizons.CTF.utils.visualizer.api.src.main.java.com.github.skjolber.packing.visualizer.api.packaging.PackagingResultVisualizer;

import java.util.List;


public interface PackagingResultVisualizerFactory<T> {

	PackagingResultVisualizer visualize(List<T> input);

}
