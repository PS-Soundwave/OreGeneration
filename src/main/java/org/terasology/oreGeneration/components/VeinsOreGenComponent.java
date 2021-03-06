/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.oreGeneration.components;

import org.terasology.customOreGen.PDist;
import org.terasology.customOreGen.StructureDefinition;
import org.terasology.customOreGen.StructureNodeType;
import org.terasology.customOreGen.VeinsStructureDefinition;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

public class VeinsOreGenComponent implements Component, CustomOreGenCreator {
    public String block;
    // frequency for every 10 cubed blocks
    public float frequency = 1f;
    public float frequencyRange;
    public float motherLodeRadius = 2f;
    public float motherLodeRadiusRange = 1f;
    public float motherlodeRangeLimit = 32;
    public float motherlodeRangeLimitRange = 32;
    public float branchFrequency = 4f;
    public float branchFrequencyRange = 1f;
    public float branchInclination = 0f;
    public float branchInclinationRange = 0.1f;
    public float branchLength = 40f;
    public float branchLengthRange = 10f;
    public float branchHeightLimit = 100f;
    public float branchHeightLimitRange = 0f;
    public float density = 1f;
    public float densityRange = 0f;
    private float segmentForkFrequency = 0.3f;
    private float segmentForkFrequencyRange = 0.1f;
    private float segmentForkLengthMultiplier = 0.25f;
    private float segmentForkLengthMultiplierRange = 0f;
    private float segmentLength = 5f;
    private float segmentLengthRange = 0f;
    private float segmentAngle = 0.5f;
    private float segmentAngleRange = 0.5f;
    private float segmentRadius = 4f;
    private float segmentRadiusRange = 1f;
    private float blockRadiusMultiplier = 1f;
    private float blockRadiusMultiplierRange = 0f;
    // This is the density at which ore generation will stop generating. Useful to prevent generating ores above the surface
    public int densityCutoff = 2;
/*


    private float segmentForkFrequency = 0.2f;
    private float segmentForkFrequencyRange;
    private float segmentForkLengthMultiplier = 0.75f;
    private float segmentForkLengthMultiplierRange = 0.25f;
    private float segmentLength = 15f;
    private float segmentLengthRange = 6f;
    private float segmentAngle = 0.5f;
    private float segmentAngleRange = 0.5f;
    private float segmentRadius = 0.5f;
    private float segmentRadiusRange = 0.3f;
    private float blockRadiusMultiplier = 1f;
    private float blockRadiusMultiplierRange = 0.1f;
 */


    @Override
    public StructureDefinition createStructureDefinition(GeneratingRegion region) {
        if (!isInRange(region)) {
            return null;
        }

        Vector3i regionSize = region.getRegion().size();
        float scaleFactor = regionSize.getY() / 10f;

        StructureDefinition structureDefinition = getStructureDefinition(region, scaleFactor);

        return structureDefinition;
    }

    protected boolean isInRange(GeneratingRegion region) {
        // find the average surface height
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        float[] values = surfaceHeightFacet.getInternal();
        float total = 0;
        // averaging every single value takes too much time, only use some of the values in our average
        int sampleRate = 4;
        for (int i = 0; i < values.length; i = i + sampleRate) {
            total += values[i];
        }
        float averageSurfaceHeight = total / (values.length / sampleRate);

        // see if this region is even in range of this ore gen
        return region.getRegion().minY() < averageSurfaceHeight;
    }

    private StructureDefinition getStructureDefinition(GeneratingRegion region, float scaleFactor) {
        return new VeinsStructureDefinition(
                new PDist(frequency * scaleFactor, frequencyRange * scaleFactor),
                new PDist(motherLodeRadius, motherLodeRadiusRange),
                new PDist(motherlodeRangeLimit, motherlodeRangeLimitRange),
                new PDist(branchFrequency, branchFrequencyRange),
                new PDist(branchInclination, branchInclinationRange),
                new PDist(branchLength, branchLengthRange),
                new PDist(branchHeightLimit, branchHeightLimitRange),
                new PDist(segmentForkFrequency, segmentForkFrequencyRange),
                new PDist(segmentForkLengthMultiplier, segmentForkLengthMultiplierRange),
                new PDist(segmentLength, segmentLengthRange),
                new PDist(segmentAngle, segmentAngleRange),
                new PDist(segmentRadius, segmentRadiusRange),
                new PDist(density, densityRange),
                new PDist(blockRadiusMultiplier, blockRadiusMultiplierRange));
    }

    @Override
    public boolean canReplaceBlock(Vector3i worldPosition, Region region) {
        DensityFacet densityFacet = region.getFacet(DensityFacet.class);
        float densityFacetValue = densityFacet.getWorld(worldPosition);
        return densityFacetValue > densityCutoff;
    }

    @Override
    public Block getReplacementBlock(StructureNodeType structureNodeType) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return blockManager.getBlock(block);
    }

    @Override
    public int getSalt() {
        return block.hashCode();
    }
}
