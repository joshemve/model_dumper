package com.modeldumper.exporter;

import com.modeldumper.ModelDumper;
import com.modeldumper.util.ModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJExporter {

    private static class Vertex {
        float x, y, z;
        float u, v;
        float nx, ny, nz;

        public Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return Float.compare(vertex.x, x) == 0 &&
                   Float.compare(vertex.y, y) == 0 &&
                   Float.compare(vertex.z, z) == 0 &&
                   Float.compare(vertex.u, u) == 0 &&
                   Float.compare(vertex.v, v) == 0 &&
                   Float.compare(vertex.nx, nx) == 0 &&
                   Float.compare(vertex.ny, ny) == 0 &&
                   Float.compare(vertex.nz, nz) == 0;
        }

        @Override
        public int hashCode() {
            int result = Float.hashCode(x);
            result = 31 * result + Float.hashCode(y);
            result = 31 * result + Float.hashCode(z);
            result = 31 * result + Float.hashCode(u);
            result = 31 * result + Float.hashCode(v);
            return result;
        }
    }

    public static void exportEntityModel(ModelData modelData, File outputFile, File mtlFile) {
        try (BufferedWriter objWriter = new BufferedWriter(new FileWriter(outputFile));
             BufferedWriter mtlWriter = new BufferedWriter(new FileWriter(mtlFile))) {

            EntityModel<?> model = modelData.model;

            objWriter.write("# Model: " + modelData.name + "\n");
            objWriter.write("mtllib " + mtlFile.getName() + "\n\n");

            List<Vertex> vertices = new ArrayList<>();

            // Extract vertices from model using reflection
            PoseStack poseStack = new PoseStack();

            try {
                // Get the root of the model
                java.lang.reflect.Method rootMethod = model.getClass().getMethod("root");
                ModelPart root = (ModelPart) rootMethod.invoke(model);
                extractModelPart(root, poseStack, vertices);
            } catch (Exception e) {
                ModelDumper.LOGGER.error("Could not extract model parts for: " + modelData.name, e);
            }

            // Write vertices
            for (Vertex v : vertices) {
                objWriter.write(String.format("v %.6f %.6f %.6f\n", v.x, v.y, v.z));
            }
            objWriter.write("\n");

            // Write texture coordinates
            for (Vertex v : vertices) {
                objWriter.write(String.format("vt %.6f %.6f\n", v.u, v.v));
            }
            objWriter.write("\n");

            // Write normals
            for (Vertex v : vertices) {
                objWriter.write(String.format("vn %.6f %.6f %.6f\n", v.nx, v.ny, v.nz));
            }
            objWriter.write("\n");

            // Write faces (triangles)
            objWriter.write("usemtl material0\n");
            for (int i = 0; i < vertices.size(); i += 3) {
                int i1 = i + 1;
                int i2 = i + 2;
                int i3 = i + 3;
                objWriter.write(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n",
                    i1, i1, i1, i2, i2, i2, i3, i3, i3));
            }

            // Write MTL file
            writeMTLFile(mtlWriter, modelData);

            ModelDumper.LOGGER.info("Exported entity model: " + modelData.name + " (" + vertices.size() + " vertices)");

        } catch (IOException e) {
            ModelDumper.LOGGER.error("Error exporting entity model: " + modelData.name, e);
        }
    }

    private static void extractModelPart(ModelPart part, PoseStack poseStack, List<Vertex> vertices) {
        poseStack.pushPose();

        // Apply part transformations
        poseStack.translate(part.x / 16.0f, part.y / 16.0f, part.z / 16.0f);
        poseStack.mulPose(new org.joml.Quaternionf().rotationXYZ(part.xRot, part.yRot, part.zRot));

        // Extract cubes
        for (ModelPart.Cube cube : part.cubes) {
            extractCube(cube, poseStack, vertices);
        }

        // Recursively extract children
        for (ModelPart child : part.children.values()) {
            extractModelPart(child, poseStack, vertices);
        }

        poseStack.popPose();
    }

    private static void extractCube(ModelPart.Cube cube, PoseStack poseStack, List<Vertex> vertices) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (ModelPart.Polygon polygon : cube.polygons) {
            ModelPart.Vertex[] polyVerts = polygon.vertices;

            // Convert quads to triangles
            if (polyVerts.length >= 3) {
                for (int i = 1; i < polyVerts.length - 1; i++) {
                    addVertex(polyVerts[0], pose, normal, vertices);
                    addVertex(polyVerts[i], pose, normal, vertices);
                    addVertex(polyVerts[i + 1], pose, normal, vertices);
                }
            }
        }
    }

    private static void addVertex(ModelPart.Vertex vertex, Matrix4f pose, Matrix3f normal, List<Vertex> vertices) {
        Vector4f pos = new Vector4f(vertex.pos.x() / 16.0f, vertex.pos.y() / 16.0f, vertex.pos.z() / 16.0f, 1.0f);
        pos = pose.transform(pos);

        Vector3f norm = new Vector3f(vertex.normal.x(), vertex.normal.y(), vertex.normal.z());
        norm = normal.transform(norm);

        Vertex v = new Vertex(
            pos.x, pos.y, pos.z,
            vertex.u, vertex.v,
            norm.x, norm.y, norm.z
        );
        vertices.add(v);
    }

    public static void exportItemModel(ModelData modelData, File outputFile, File mtlFile) {
        try (BufferedWriter objWriter = new BufferedWriter(new FileWriter(outputFile));
             BufferedWriter mtlWriter = new BufferedWriter(new FileWriter(mtlFile))) {

            objWriter.write("# Item Model: " + modelData.name + "\n");
            objWriter.write("mtllib " + mtlFile.getName() + "\n\n");

            List<Vertex> vertices = new ArrayList<>();

            // Extract quads from baked model
            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = modelData.bakedModel.getQuads(null, direction, null);
                extractQuads(quads, vertices);
            }

            // Also get quads without direction
            List<BakedQuad> quads = modelData.bakedModel.getQuads(null, null, null);
            extractQuads(quads, vertices);

            // Write OBJ data
            for (Vertex v : vertices) {
                objWriter.write(String.format("v %.6f %.6f %.6f\n", v.x, v.y, v.z));
            }
            objWriter.write("\n");

            for (Vertex v : vertices) {
                objWriter.write(String.format("vt %.6f %.6f\n", v.u, 1.0f - v.v));
            }
            objWriter.write("\n");

            for (Vertex v : vertices) {
                objWriter.write(String.format("vn %.6f %.6f %.6f\n", v.nx, v.ny, v.nz));
            }
            objWriter.write("\n");

            objWriter.write("usemtl material0\n");
            for (int i = 0; i < vertices.size(); i += 3) {
                int i1 = i + 1;
                int i2 = i + 2;
                int i3 = i + 3;
                objWriter.write(String.format("f %d/%d/%d %d/%d/%d %d/%d/%d\n",
                    i1, i1, i1, i2, i2, i2, i3, i3, i3));
            }

            writeMTLFile(mtlWriter, modelData);

            ModelDumper.LOGGER.debug("Exported item model: " + modelData.name + " (" + vertices.size() + " vertices)");

        } catch (IOException e) {
            ModelDumper.LOGGER.error("Error exporting item model: " + modelData.name, e);
        }
    }

    private static void extractQuads(List<BakedQuad> quads, List<Vertex> vertices) {
        for (BakedQuad quad : quads) {
            int[] vertexData = quad.getVertices();

            // Vertex format: x, y, z, color, u, v, lightmap, normal
            // Each vertex is 8 integers (32 bytes)
            int vertexSize = vertexData.length / 4;

            Vertex[] quadVerts = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                float x = Float.intBitsToFloat(vertexData[offset]);
                float y = Float.intBitsToFloat(vertexData[offset + 1]);
                float z = Float.intBitsToFloat(vertexData[offset + 2]);
                float u = Float.intBitsToFloat(vertexData[offset + 4]);
                float v = Float.intBitsToFloat(vertexData[offset + 5]);

                // Extract normal from packed int
                int packedNormal = vertexData[offset + 7];
                float nx = ((byte) (packedNormal & 0xFF)) / 127.0f;
                float ny = ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0f;
                float nz = ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0f;

                quadVerts[i] = new Vertex(x, y, z, u, v, nx, ny, nz);
            }

            // Convert quad to two triangles
            vertices.add(quadVerts[0]);
            vertices.add(quadVerts[1]);
            vertices.add(quadVerts[2]);

            vertices.add(quadVerts[0]);
            vertices.add(quadVerts[2]);
            vertices.add(quadVerts[3]);
        }
    }

    private static void writeMTLFile(BufferedWriter writer, ModelData modelData) throws IOException {
        writer.write("# Material for " + modelData.name + "\n");
        writer.write("newmtl material0\n");
        writer.write("Ka 1.0 1.0 1.0\n");
        writer.write("Kd 1.0 1.0 1.0\n");
        writer.write("Ks 0.0 0.0 0.0\n");
        writer.write("d 1.0\n");
        writer.write("illum 1\n");

        String textureName = modelData.name + ".png";
        writer.write("map_Kd " + textureName + "\n");
    }

    private static class VertexCollector implements VertexConsumer {
        private final List<Vertex> vertices;
        private float x, y, z, u, v, nx, ny, nz;

        public VertexCollector(List<Vertex> vertices) {
            this.vertices = vertices;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.nx = x;
            this.ny = y;
            this.nz = z;
            return this;
        }

        @Override
        public void endVertex() {
            vertices.add(new Vertex(x, y, z, u, v, nx, ny, nz));
        }
    }
}
