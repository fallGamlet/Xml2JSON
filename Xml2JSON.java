import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Xml2JSON {
    public static final String NODE_FILED_NAME = "name";
    public static final String NODE_FILED_CONTENT = "content";
    
    public static JSONObject getJSON(Node node) throws JSONException {
        if (node == null) { return null; }
        int type = node.getNodeType();
        int textType = Node.TEXT_NODE;
        int nodeType = Node.ELEMENT_NODE;

        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("name", node.getNodeName());

        if (node.hasAttributes()) {
            JSONObject jsonAttr = new JSONObject();
            NamedNodeMap attr = node.getAttributes();
            int attrLenth = attr.getLength();
            for (int i=0; i<attrLenth; i++) {
                Node attrItem = attr.item(i);
                String name = attrItem.getNodeName();
                String value = attrItem.getNodeValue();
                jsonAttr.putOpt(name, value);
            }
            jsonObject.putOpt("attr", jsonAttr);
        }

        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            int childrenCount = children.getLength();

            if (childrenCount == 1) {
                Node item = children.item(0);
                int itemType = item.getNodeType();
                if (itemType == Node.TEXT_NODE) {
                    jsonObject.putOpt("content", item.getNodeValue());
                    return jsonObject;
                }
            }

            for (int i=0; i<childrenCount; i++) {
                Node item = children.item(i);
                int itemType = item.getNodeType();
                if (itemType == Node.DOCUMENT_NODE || itemType == Node.ELEMENT_NODE) {
                    JSONObject jsonItem = getJSON(item);
                    if (jsonItem != null) {
                        String name = jsonItem.optString(NODE_FILED_NAME);

                        if (!jsonObject.has(NODE_FILED_CONTENT)) { jsonObject.putOpt(NODE_FILED_CONTENT, new JSONObject()); }
                        JSONObject jsonContent = jsonObject.optJSONObject(NODE_FILED_CONTENT);

                        Object jsonByName = jsonContent.opt(name);
                        if (jsonByName == null) {
                            jsonContent.putOpt(name, jsonItem);
                        } else if (jsonByName instanceof JSONArray) {
                            ((JSONArray)jsonByName).put(jsonItem);
                        } else {
                            JSONArray arr = new JSONArray();
                            arr.put(jsonByName);
                            arr.put(jsonItem);
                            jsonContent.putOpt(name, arr);
                        }
                    }
                }
            }
        } else {
            jsonObject.putOpt(NODE_FILED_CONTENT, node.getNodeValue());
        }

        return jsonObject;
    }

    public static Object searchItem(JSONObject src, String name) {
        if (src == null) { return null; }
        String curName = src.optString(NODE_FILED_NAME, null);
        boolean check = (curName == name)
                || (curName != null && curName.equalsIgnoreCase(name));
        if (check) { return src;}

        Object content = src.opt(NODE_FILED_CONTENT);
        if (content instanceof JSONObject) {
            JSONObject jsonContent = (JSONObject)content;

            Object resultObj = jsonContent.optJSONObject(name);
            if (resultObj != null) { return resultObj; }

            Iterator<String> keys = jsonContent.keys();
            while (keys != null && keys.hasNext()) {
                String key = keys.next();
                Object contentItem = jsonContent.opt(key);
                if (contentItem != null) {
                    if (contentItem instanceof JSONObject) {
                        resultObj = searchItem((JSONObject)contentItem, name);
                        if (resultObj != null) { return resultObj; }
                    } else if (contentItem instanceof JSONArray) {
                        JSONArray arr = (JSONArray) contentItem;
                        int count = arr.length();
                        for (int i=0; i<count; i++) {
                            Object arrItem = arr.opt(i);
                            if (arrItem instanceof JSONObject) {
                                resultObj = searchItem((JSONObject)arrItem, name);
                                if (resultObj != null) { return resultObj; }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
