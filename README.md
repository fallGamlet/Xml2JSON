# Xml2JSON
Java code for convert Xml Node to JSON
It's simple class with methods:
<ol>
<li><b>getJSON</b> - get Xml Node element end convert into JSONObject</li>
<li><b>getXmlString</b> - Simple convert JSON object to XML string without attributes. Every key it is tag,every value of key is content (recursive)</li>
<li><b>searchItemWithName</b> - found element in source JSONObject by field 'name'</li>
<li><b>searchItemFromField</b> - Get the object that is to be under the specified field</li>
<li><b>makeShortWithoutAttr</b> - Make JSON object shorter with remove subfiled if subfield value is not JSONObject or JSONArray and remove attributes (attr)</li>
<ol>
