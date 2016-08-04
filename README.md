# Xml2JSON
Java code for convert Xml Node to JSON
It's simple class with 2 methods:
<br>1) getJSON - get Xml Node element end convert into JSONObject
<br>2) getXmlString - Simple convert JSON object to XML string without attributes. Every key it is tag,every value of key is content (recursive)
<br>3) searchItemWithName - found element in source JSONObject by field 'name'
<br>4) searchItemFromField - Get the object that is to be under the specified field
<br>5) makeShortWithoutAttr - Make JSON object shorter with remove subfiled if subfield value is not JSONObject or JSONArray and remove attributes (attr)
