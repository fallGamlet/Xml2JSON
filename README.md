# Xml2JSON
Java code for convert Xml Node to JSON
It's simple class with methods:
<ol>
<li>getJSON - get Xml Node element end convert into JSONObject</li>
<li> getXmlString - Simple convert JSON object to XML string without attributes. Every key it is tag,every value of key is content (recursive)</li>
<li> searchItemWithName - found element in source JSONObject by field 'name'</li>
<li> searchItemFromField - Get the object that is to be under the specified field</li>
<li> makeShortWithoutAttr - Make JSON object shorter with remove subfiled if subfield value is not JSONObject or JSONArray and remove attributes (attr)</li>
<ol>
