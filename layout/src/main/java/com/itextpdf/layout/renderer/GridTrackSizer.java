/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.layout.renderer;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.properties.GridValue;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.renderer.Grid.GridOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// 12.3. Track Sizing Algorithm
class GridTrackSizer {
    private static final float EPSILON = 0.001f;

    private final Grid grid;
    private final List<Track> tracks;
    private final float availableSpace;
    private final GridOrder order;
    private final Collection<GridCell> cachedUniqueGridCells;

    GridTrackSizer(Grid grid, List<GridValue> values, float gap, float availableSpace, GridOrder order) {
        this.grid = grid;
        // Cache the result of the getUniqueGridCells to speed up calculations
        this.cachedUniqueGridCells = grid.getUniqueGridCells(order);
        tracks = new ArrayList<>(values.size());
        for (GridValue value : values) {
            final Track track = new Track();
            track.value = value;
            tracks.add(track);
        }
        if (availableSpace < 0) {
            for (Track track : tracks) {
                if (track.value.isPercentValue()) {
                    // 7.2.1. Track Sizes: If the size of the grid container depends on the
                    // size of its tracks, then the <percentage> must be treated as auto
                    track.value = GridValue.createAutoValue();
                }
            }
        }
        // Grid sizing algorithm says "Gutters are treated as empty fixed-size tracks for the purpose of the algorithm."
        // But relative gaps haven't supported yet, it is why to make algorithm simpler, available space just reduced by gaps.
        this.availableSpace = availableSpace - ((values.size() - 1) * gap);

        this.order = order;
    }

    List<Float> sizeTracks() {
        // First step (12.4. Initialize Track Sizes)
        initializeTrackSizes();
        // Second step (12.5. Resolve Intrinsic Track Sizes)
        resolveIntrinsicTrackSizes();
        // Third step (12.6. Maximize Tracks)
        maximizeTracks();
        // Fourth step (12.7. Expand Flexible Tracks)
        expandFlexibleTracks();
        // Fifth step (12.8. Stretch auto Tracks)
        // Skip for now

        List<Float> result = new ArrayList<>(tracks.size());
        for (Track track : tracks) {
            result.add(track.baseSize);
        }
        return result;
    }

    private void maximizeTracks() {
        Float freeSpace = getFreeSpace();
        if (freeSpace != null) {
            float leftSpace = (float) freeSpace;
            while (leftSpace > EPSILON) {
                int unfrozenTracks = 0;
                for (Track track : tracks) {
                    if (Float.compare(track.baseSize, track.growthLimit) < 0) {
                        unfrozenTracks++;
                    }
                }
                if (unfrozenTracks == 0) {
                    break;
                }
                float diff = leftSpace / unfrozenTracks;
                for (Track track : tracks) {
                    if (Float.compare(track.baseSize, track.growthLimit) < 0) {
                        float trackDiff = Math.min(track.growthLimit, track.baseSize + diff) - track.baseSize;
                        track.baseSize += trackDiff;
                        leftSpace -= trackDiff;
                    }
                }
            }
        } else {
            for (Track track : tracks) {
                if (Float.compare(track.baseSize, track.growthLimit) < 0) {
                    track.baseSize = track.growthLimit;
                }
            }
        }
    }


    private void expandFlexibleTracks() {
        boolean thereIsFlexibleTrack = false;
        for (Track track : tracks) {
            if (track.value.isFlexibleValue()) {
                thereIsFlexibleTrack = true;
                break;
            }
        }
        if (!thereIsFlexibleTrack) {
            return;
        }

        float frSize = 0;
        if (availableSpace >= 0) {
            // If the free space is zero or if sizing the grid container under a min-content constraint:
            float freeSpace = (float) getFreeSpace();
            if (freeSpace < EPSILON) {
                return;
            }

            // Otherwise, if the free space is a definite length:
            frSize = findFrSize(tracks, availableSpace);
        } else {
            // Otherwise, if the free space is an indefinite length:
            for (Track track : tracks) {
                if (track.value.isFlexibleValue()) {
                    frSize = Math.max(frSize, (float) (track.baseSize / track.value.getValue()));
                }
            }
            for (GridCell cell : cachedUniqueGridCells) {
                boolean atLeastOneFlexTrack = false;
                List<Track> affectedTracks = getAffectedTracks(cell);
                for (Track track : affectedTracks) {
                    if (track.value.isFlexibleValue()) {
                        atLeastOneFlexTrack = true;
                    }
                }
                if (!atLeastOneFlexTrack) {
                    continue;
                }
                float maxContribution = calculateMaxContribution(cell, order);
                frSize = Math.max(frSize, findFrSize(affectedTracks, maxContribution));
            }
        }
        for (Track track : tracks) {
            if (track.value.isFlexibleValue()) {
                float newBaseSize = frSize * (float) track.value.getValue();
                if (newBaseSize > track.baseSize) {
                    track.baseSize = newBaseSize;
                }
            }
        }
    }

    private List<Track> getAffectedTracks(GridCell cell) {
        List<Track> affectedTracks = new ArrayList<>();
        for (int i = cell.getStart(order); i < cell.getEnd(order); i++) {
            affectedTracks.add(tracks.get(i));
        }
        return affectedTracks;
    }

    private Float getFreeSpace() {
        if (availableSpace < 0) {
            return null;
        }
        float freeSpace = availableSpace;
        for (Track track : tracks) {
            freeSpace -= track.baseSize;
        }
        return freeSpace;
    }

    private static float findFrSize(List<Track> affectedTracks, float spaceToFill) {
        // 12.7.1. Find the Size of an fr
        float frSize = 0;
        boolean allFlexTracksSatisfied = false;
        boolean[] ignoreTracks = new boolean[affectedTracks.size()];
        while (!allFlexTracksSatisfied) {
            float leftoverSpace = spaceToFill;
            float flexFactorSum = 0;
            for (int i = 0; i < affectedTracks.size(); i++) {
                Track track = affectedTracks.get(i);
                if (track.value.isFlexibleValue() && !ignoreTracks[i]) {
                    flexFactorSum += (float) track.value.getValue();
                } else {
                    leftoverSpace -= track.baseSize;
                }
            }
            flexFactorSum = flexFactorSum < 1 ? 1 : flexFactorSum;
            float hyphFrSize = leftoverSpace / flexFactorSum;

            allFlexTracksSatisfied = true;
            for (int i = 0; i < affectedTracks.size(); i++) {
                Track track = affectedTracks.get(i);
                if (track.value.isFlexibleValue() && !ignoreTracks[i]) {
                    if (hyphFrSize * track.value.getValue() < track.baseSize) {
                        ignoreTracks[i] = true;
                        allFlexTracksSatisfied = false;
                    }
                }
            }
            if (allFlexTracksSatisfied) {
                frSize = hyphFrSize;
            }
        }
        return frSize;
    }

    private void resolveIntrinsicTrackSizes() {
        // 1. Shim baseline-aligned items so their intrinsic size contributions reflect their baseline alignment.
        // Not sure whether we need to do anything in first point

        // 2. Size tracks to fit non-spanning items.
        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            // TODO DEVSIX-8384 percent value can be resolvable for height if height of grid container is specified
            if (track.value.isPointValue() || track.value.isPercentValue()) {
                continue;
            }
            Collection<GridCell> cells = grid.getUniqueCellsInTrack(order, i);
            // -> For max-content minimums:
            if (track.value.isMaxContentValue()) {
                float maxContribution = 0;
                for (GridCell cell : cells) {
                    // non-spanning items only
                    if (cell.getGridSpan(order) == 1) {
                        float contribution = calculateMaxContribution(cell, order);
                        maxContribution = Math.max(maxContribution, contribution);
                    }
                }
                track.baseSize = maxContribution;
            }

            // -> For min-content minimums:
            // -> For auto minimums: (also the case if track specified by fr value)
            if (track.value.isAutoValue() || track.value.isFlexibleValue() || track.value.isMinContentValue()) {
                float maxContribution = 0;
                for (GridCell cell : cells) {
                    // non-spanning items only
                    if (cell.getGridSpan(order) == 1) {
                        float contribution = calculateMinContribution(cell, order);
                        maxContribution = Math.max(maxContribution, contribution);
                    }
                }
                track.baseSize = maxContribution;
            }
            // -> For min-content maximums:
            if (track.value.isMinContentValue() && track.baseSize > EPSILON) {
                track.growthLimit = track.baseSize;
            }
            // -> For max-content maximums:
            // Treat auto as max-content for max track sizing function
            if (track.value.isAutoValue() || track.value.isMaxContentValue()) {
                float maxContribution = 0;
                for (GridCell cell : cells) {
                    // non-spanning items only
                    if (cell.getGridSpan(order) == 1) {
                        float contribution = calculateMaxContribution(cell, order);
                        maxContribution = Math.max(maxContribution, contribution);
                    }
                }
                if (maxContribution > EPSILON) {
                    track.growthLimit = maxContribution;
                }
            }

            // if a track’s growth limit is now less than its base size
            if (track.growthLimit > 0 && track.baseSize > EPSILON && track.baseSize > track.growthLimit) {
                track.growthLimit = track.baseSize;
            }
        }

        // 3. Increase sizes to accommodate spanning items crossing content-sized tracks.
        int maxSpanCell = 0;
        for (GridCell cell : cachedUniqueGridCells) {
            maxSpanCell = Math.max(maxSpanCell, cell.getGridSpan(order));
        }
        for (int span = 2; span <= maxSpanCell; span++) {
            for (GridCell cell : cachedUniqueGridCells) {
                if (cell.getGridSpan(order) == span) {
                    boolean flexTracksExist = false;
                    List<Track> affectedTracks = getAffectedTracks(cell);
                    for (Track track : affectedTracks) {
                        if (track.value.isFlexibleValue()) {
                            flexTracksExist = true;
                        }
                    }
                    if (flexTracksExist) {
                        continue;
                    }
                    float contribution = calculateMinContribution(cell, order);
                    // 3.1 For intrinsic minimums:
                    // 3.2 For content-based minimums:
                    // 3.3 For max-content minimums:
                    distributeExtraSpace(affectedTracks, true, contribution);
                }
            }
            // 3.4 If at this point any track’s growth limit is now less than its base size:
            // 3.5 For intrinsic maximums:
            // 3.6 For max-content maximums:
        }

        // 4. Increase sizes to accommodate spanning items crossing flexible tracks:
        for (GridCell cell : cachedUniqueGridCells) {
            boolean atLeastOneFlexTrack = false;
            List<Track> affectedTracks = new ArrayList<>();
            for (int i = cell.getStart(order); i < cell.getEnd(order); i++) {
                if (tracks.get(i).value.isFlexibleValue()) {
                    atLeastOneFlexTrack = true;
                    affectedTracks.add(tracks.get(i));
                }
            }
            if (!atLeastOneFlexTrack) {
                continue;
            }
            float contribution = calculateMinContribution(cell, order);
            distributeExtraSpaceWithFlexTracks(affectedTracks, contribution);
        }

        // 5. If any track still has an infinite growth limit
        for (Track track : tracks) {
            if (track.growthLimit < 0) {
                track.growthLimit = track.baseSize;
            }
        }
    }

    private void distributeExtraSpaceWithFlexTracks(List<Track> tracks, float sizeContribution) {
        // 1. Find the space to distribute:
        float trackSizes = 0;
        float sumFraction = 0;
        for (Track track : tracks) {
            trackSizes += track.baseSize;
            if (track.value.isFlexibleValue()) {
                sumFraction += (float) track.value.getValue();
            }
        }
        float space = Math.max(0, sizeContribution - trackSizes);
        // 2. Distribute space up to limits:
        while (space > EPSILON) {
            float distributedSpace = space / sumFraction;
            boolean allFrozen = true;
            for (Track track : tracks) {
                if (track.value.isFlexibleValue()) {
                    Float added = distributeSpaceToTrack(track, distributedSpace);
                    if (added != null) {
                        space -= (float) added;
                        allFrozen = false;
                    }
                }
            }
            if (allFrozen) {
                break;
            }
        }
        // 3. Distribute space to non-affected tracks: skipped
        // 4. Distribute space beyond limits: skipped
    }

    private void distributeExtraSpace(List<Track> tracks, boolean affectsBase, float sizeContribution) {
        // 1. Find the space to distribute:
        float trackSizes = 0;
        int numberOfAffectedTracks = 0;
        for (Track track : tracks) {
            trackSizes += affectsBase ? track.baseSize : track.growthLimit;
            if (!track.value.isPointValue() && !track.value.isPercentValue()) {
                numberOfAffectedTracks++;
            }
        }
        float space = Math.max(0, sizeContribution - trackSizes);
        // 2. Distribute space up to limits:
        while (space > EPSILON) {
            float distributedSpace = space / numberOfAffectedTracks;
            boolean allFrozen = true;
            for (Track track : tracks) {
                if (!track.value.isPointValue() && !track.value.isPercentValue()) {
                    Float added = distributeSpaceToTrack(track, distributedSpace);
                    if (added != null) {
                        space -= (float) added;
                        allFrozen = false;
                    }
                }
            }
            if (allFrozen) {
                break;
            }
        }
        // 3. Distribute space to non-affected tracks: skipped
        // 4. Distribute space beyond limits: skipped
    }

    private void initializeTrackSizes() {
        for (Track track : tracks) {
            // A fixed sizing function

            // TODO DEVSIX-8384 percent value can be resolvable for height if height of grid container is specified
            if (track.value.isPointValue() || track.value.isPercentValue()) {
                if (track.value.isPointValue()) {
                    track.baseSize = (float) track.value.getValue();
                } else {
                    track.baseSize = (float) track.value.getValue() / 100 * availableSpace;
                }
                track.growthLimit = track.baseSize;
            } else {
                track.baseSize = 0;
                track.growthLimit = -1;
            }
        }
    }

    private static Float distributeSpaceToTrack(Track track, float distributedSpace) {
        if (track.growthLimit < 0 || distributedSpace + track.baseSize <= track.growthLimit) {
            track.baseSize += distributedSpace;
            return distributedSpace;
        } else if (Float.compare(track.growthLimit, track.baseSize) != 0) {
            float addedToLimit = track.growthLimit - track.baseSize;
            track.baseSize += addedToLimit;
            return addedToLimit;
        }
        return null;
    }

    private static float calculateMinContribution(GridCell cell, GridOrder order) {
        if (GridOrder.COLUMN == order) {
            if (cell.getValue() instanceof AbstractRenderer) {
                AbstractRenderer abstractRenderer = (AbstractRenderer) cell.getValue();
                return abstractRenderer.getMinMaxWidth().getMinWidth();
            }
        } else {
            cell.getValue().setProperty(Property.FILL_AVAILABLE_AREA, Boolean.FALSE);
            final LayoutContext layoutContext = new LayoutContext(
                    new LayoutArea(1, new Rectangle(cell.getLayoutArea().getWidth(), AbstractRenderer.INF)));
            LayoutResult inifiniteHeighLayoutResult = cell.getValue().layout(layoutContext);
            if (inifiniteHeighLayoutResult.getStatus() == LayoutResult.NOTHING
                    || inifiniteHeighLayoutResult.getStatus() == LayoutResult.PARTIAL) {
                return 0;
            }
            return inifiniteHeighLayoutResult.getOccupiedArea().getBBox().getHeight();
        }
        return 0;
    }

    private static float calculateMaxContribution(GridCell cell, GridOrder gridOrder) {
        if (GridOrder.COLUMN == gridOrder) {
            if (cell.getValue() instanceof AbstractRenderer) {
                AbstractRenderer abstractRenderer = (AbstractRenderer) cell.getValue();
                return abstractRenderer.getMinMaxWidth().getMaxWidth();
            }
        } else {
            cell.getValue().setProperty(Property.FILL_AVAILABLE_AREA, Boolean.FALSE);
            // https://drafts.csswg.org/css-sizing-3/#auto-box-sizes:
            // min-content block size - For block containers, tables, and
            // inline boxes, this is equivalent to the max-content block size.
            final LayoutContext layoutContext = new LayoutContext(
                    new LayoutArea(1, new Rectangle(cell.getLayoutArea().getWidth(), AbstractRenderer.INF)));
            LayoutResult inifiniteHeighLayoutResult = cell.getValue().layout(layoutContext);
            if (inifiniteHeighLayoutResult.getStatus() == LayoutResult.NOTHING
                    || inifiniteHeighLayoutResult.getStatus() == LayoutResult.PARTIAL) {
                cell.setValueFitOnCellArea(false);
                return 0;
            }
            return inifiniteHeighLayoutResult.getOccupiedArea().getBBox().getHeight();
        }
        return 0;
    }

    private static class Track {
        float baseSize;
        // consider -1 as an infinity value
        float growthLimit;
        GridValue value;
    }
}
