package com.gtnewhorizons.CTF.utils.visualizer.api.src.main.java.com.github.skjolber.packing.visualizer.api.packaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


public class PackagingResultVisualizer {

	private List<ContainerVisualizer> containers = new ArrayList<>();

	public List<ContainerVisualizer> getContainers() {
		return containers;
	}

	public void setContainers(List<ContainerVisualizer> containers) {
		this.containers = containers;
	}

	public boolean add(ContainerVisualizer e) {
		return containers.add(e);
	}

	public String toJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
	}

}
