package jaredbgreat.climaticbiome.generation.map;

import jaredbgreat.climaticbiome.generation.chunk.BasinNode;

/**
 * Thread-local buffers holding preallocated {@link BasinNode} arrays used
 * during terrain generation to avoid repeated allocations.
 */
final class TerrainBufferPool {
    private static final int TERRAIN_BUFFER_SIZE = 7 * 7;
    private static final ThreadLocal<TerrainBufferPool> LOCAL
            = ThreadLocal.withInitial(TerrainBufferPool::new);

    static TerrainBufferPool local() {
        return LOCAL.get();
    }

    private final BasinNode[] heightNodes;
    private final BasinNode[] scaleNodes;

    private TerrainBufferPool() {
        heightNodes = new BasinNode[TERRAIN_BUFFER_SIZE];
        scaleNodes = new BasinNode[TERRAIN_BUFFER_SIZE];
        initialize(heightNodes);
        initialize(scaleNodes);
    }

    BasinNode[] acquireHeightNodes() {
        return heightNodes;
    }

    BasinNode[] acquireScaleNodes() {
        return scaleNodes;
    }

    void resetTerrainNodes() {
        resetNodes(heightNodes);
        resetNodes(scaleNodes);
    }

    private void initialize(BasinNode[] nodes) {
        for(int i = 0; i < nodes.length; i++) {
            nodes[i] = new BasinNode(0, 0, 0.0, 1.0);
        }
    }

    private void resetNodes(BasinNode[] nodes) {
        for(int i = 0; i < nodes.length; i++) {
            nodes[i].reset(0, 0, 0.0, 1.0);
        }
    }
}
