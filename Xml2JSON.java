package ru.hellix.fallgamlet.fitness.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by fallgamlet on 04.07.16.
 */
public class Xml2JSON {
    public static final String NODE_FILED_ATTR = "attr";
    public static final String NODE_FILED_NAME = "name";
    public static final String NODE_FILED_CONTENT = "content";

    /**
     * Get JSON object from Xml Node (use recursion)
     * @param node Src Xml Node
     * @return JSONObject if OK and null if can't parse. JSON object has fields: attr - attribures of node; name - name of tag, content - include values (may be String, JSONArray, JSONObject or NULL)
     * @throws JSONException
     */
    public static JSONObject getJSON(Node node) throws JSONException {
        if (node == null) { return null; }
        int type = node.getNodeType();
        int textType = Node.TEXT_NODE;
        int nodeType = Node.ELEMENT_NODE;

        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt(NODE_FILED_NAME, node.getNodeName());

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
            jsonObject.putOpt(NODE_FILED_ATTR, jsonAttr);
        }

        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            int childrenCount = children.getLength();

            if (childrenCount == 1) {
                Node item = children.item(0);
                int itemType = item.getNodeType();
                if (itemType == Node.TEXT_NODE) {
                    jsonObject.putOpt(NODE_FILED_CONTENT, item.getNodeValue());
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

    /**
     * Get object from src JSON by 'name' field (recursive)
     * @param src Source JSONObject
     * @param name Value of object's tag "name"
     * @return Founded object or null
     */
    public static Object searchItemWithName(JSONObject src, String name) {
        if (src == null) { return null; }
        String curName = src.optString(NODE_FILED_NAME, null);
        if (curName == null) {
            Iterator<String> keys = src.keys();
            if (keys == null || !keys.hasNext()) { return null; }
            while (keys.hasNext()) {
                String key = keys.next();
                boolean check = (key == name)
                        || (key != null && key.equalsIgnoreCase(name));
                if (check) { return src.opt(key);}
            }
        }

        boolean check = (curName == name)
                || (curName != null && curName.equalsIgnoreCase(name));
        if (check) { return src;}

        Object content = src.opt(NODE_FILED_CONTENT);
        if (content == null) {
            return null;
        } else if (content instanceof JSONObject) {
            JSONObject jsonContent = (JSONObject)content;

            Object resultObj = jsonContent.opt(name);
            if (resultObj != null) { return resultObj; }

            Iterator<String> keys = jsonContent.keys();
            while (keys != null && keys.hasNext()) {
                String key = keys.next();
                Object contentItem = jsonContent.opt(key);
                if (contentItem != null) {
                    if (contentItem instanceof JSONObject) {
                        resultObj = searchItemWithName((JSONObject)contentItem, name);
                        if (resultObj != null) { return resultObj; }
                    } else if (contentItem instanceof JSONArray) {
                        JSONArray arr = (JSONArray) contentItem;
                        int count = arr.length();
                        for (int i=0; i<count; i++) {
                            Object arrItem = arr.opt(i);
                            if (arrItem instanceof JSONObject) {
                                resultObj = searchItemWithName((JSONObject)arrItem, name);
                                if (resultObj != null) { return resultObj; }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the object that is to be under the specified field
     * @param src Source JSONObject or JSONArray object for start search (non recursive)
     * @param fieldName Field name for search
     * @return Founded object or null
     */
    public static Object searchItemFromField(Object src, String fieldName) {
        if (src == null || !(src instanceof JSONObject || src instanceof JSONArray) || fieldName == null) {
            return null;
        }

        LinkedList<Object> openList = new LinkedList<>();
        openList.addLast(src);

        while (!openList.isEmpty()) {
            Object oItem = openList.pollFirst();
            if (oItem instanceof JSONArray) {
                JSONArray jArr = (JSONArray) oItem;
                int len = jArr.length();
                for(int i=0; i<len; i++) {
                    Object obj = jArr.opt(i);
                    if (obj != null && (obj instanceof JSONArray || obj instanceof JSONObject)) {
                        openList.addLast(obj);
                    }
                }
            } else if (oItem instanceof JSONObject) {
                JSONObject jItem = (JSONObject) oItem;
                Iterator<String> keys = jItem.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object obj = jItem.opt(key);
                    if (key != null && key.equalsIgnoreCase(fieldName)) {
                        return obj;
                    } else if (obj != null && (obj instanceof JSONArray || obj instanceof JSONObject)) {
                        openList.addLast(obj);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Simple convert JSON object to XML string without attributes. Every key it is tag,every value of key is content (recursive)
     * @param jObj Src JSON object? must have value types only one of String, JSONObject, JSONArray
     * @return String with xml text
    */
    public static String getXmlString(JSONObject jObj) {
        if (jObj == null) { return null; }
        StringBuilder builder = new StringBuilder();


        Iterator<String> keys = jObj.keys();
        while (keys.hasNext()) {
            String paramName = keys.next();
            Object oVal = jObj.opt(paramName);

            if (oVal == null) {
                builder.append("<"); builder.append(paramName); builder.append(">");
                builder.append("</"); builder.append(paramName); builder.append(">");
            } else if (oVal instanceof JSONArray) {
                JSONArray jArr = (JSONArray) oVal;
                int len = jArr.length();
                for (int i = 0; i < len; i++) {
                    Object jArrObjItem = jArr.opt(i);
                    if (jArrObjItem != null) {
                        builder.append("<"); builder.append(paramName); builder.append(">");
                        if (jArrObjItem instanceof JSONObject) {
                            JSONObject jVal = (JSONObject) jArrObjItem;
                            String sVal = getXmlString(jVal);
                            builder.append(sVal == null ? "" : sVal);
                        } else if (jArrObjItem instanceof String) {
                            builder.append((String)jArrObjItem);
                        }
                        builder.append("</"); builder.append(paramName); builder.append(">");
                    }
                }
            } else {
                builder.append("<"); builder.append(paramName); builder.append(">");
                if (oVal instanceof JSONObject) {
                    JSONObject jVal = (JSONObject) oVal;
                    String sVal = getXmlString(jVal);
                    builder.append(sVal == null ? "" : sVal);
                } else if (oVal instanceof String) {
                    builder.append((String) oVal);
                }
                builder.append("</"); builder.append(paramName); builder.append(">");
            }
        }
        return builder.toString();
    }

    /**
     * Make JSON object shorter with remove subfiled if subfield value is not JSONObject or JSONArray and remove attributes (attr)
     * @param src Source for making short
     * @return shorted JSON or null
     */
    public static Object makeShortWithoutAttr(Object src) throws JSONException {
        if (src == null) { return null; }
        if (src instanceof JSONObject) {
            JSONObject jSrc = (JSONObject) src;
            String name = jSrc.optString(NODE_FILED_NAME, null);
            Object content = jSrc.opt(NODE_FILED_CONTENT);

            if (name == null) {
                Iterator<String> keys = jSrc.keys();
                if (!keys.hasNext()) { return null; }

                JSONObject jRes = new JSONObject();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object item = jSrc.opt(key);
                    if (item != null) {
                        Object shortedItem = makeShortWithoutAttr(item);
                        jRes.putOpt(key, shortedItem);
                    }
                }
                return jRes;
            }

            if (content == null) { return null; }
            Object shortedItem = makeShortWithoutAttr(content);
            if (shortedItem == null) { return null; }
            return shortedItem;

        } else if (src instanceof JSONArray) {
            JSONArray jarrSrc = (JSONArray) src;
            int count = jarrSrc.length();
            if (count == 0) { return null; }
            JSONArray jarrRes = new JSONArray();
            for (int i=0; i<count; i++) {
                Object oItem = jarrSrc.opt(i);
                if (oItem != null) {
                    Object shortedItem = makeShortWithoutAttr(oItem);
                    if (shortedItem != null) { jarrRes.put(shortedItem); }
                }
            }
            return jarrRes;
        }
        return src;
    }
}
