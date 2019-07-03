package ua.com.foxminded.sanitizer;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;

public class PatchDataJsonDeserializer extends StdDeserializer<PatchData> {
    private static final long serialVersionUID = -1416645044724425202L;

    public PatchDataJsonDeserializer() {
        this(null);
    }

    public PatchDataJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PatchData deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        PatchData patchData = new PatchData();
        JsonNode node = p.readValueAsTree();

        String originalFileName = node.get("originalFileName").asText();
        String processedFileName = node.get("processedFileName").asText();
        int originalFileHashCode = (Integer) ((IntNode) node.get("originalFileHashCode")).numberValue();
        int processedFileHashCode = (Integer) ((IntNode) node.get("processedFileHashCode")).numberValue();
        Map<Integer, Delta> patches = new LinkedHashMap<Integer, Delta>();

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> patchDataIter = node.fields();
            while (patchDataIter.hasNext()) {
                Map.Entry<String, JsonNode> patchDataEntryNode = patchDataIter.next();
                JsonNode jsonNode = patchDataEntryNode.getValue();
                System.out.println(jsonNode);
            }
        }

        patchData.setOriginalFileHashCode(originalFileHashCode);
        patchData.setProcessedFileHashCode(processedFileHashCode);
        patchData.setOriginalFileName(originalFileName);
        patchData.setProcessedFileName(processedFileName);
        patchData.setPatches(patches);

        return patchData;
    }

}
