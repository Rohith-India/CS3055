# CS3055

Mini Project to implement "Extraction and analysis of subgraphs from online networks"

Progresss:

Jan 9 - Jan 30

Implemented REST API to fetch the connected authors based on the static XML data.

Input (Names of persons) is accepted using JSONArray and output of connected graph is returned using indices.

Jan 31 - Feb 15

Reading Persons information directly using https://dblp.org/pid/<pid>.xml file
  
Input (pids) is accepted using JSONArray and output of connected graph is returned using indices.

Feb 16 - Feb 24

1. First time, static block loads the PersonNameIdMap by reading dblp.xml.gz, Update Person Name and pid as Key value pairs and write them into Persons.csv file
  
2. If Persons.csv exists read the file and create Name - pid map
  
3. Read Input (Names of persons) using a text input file "Input.txt), read the pids from Name - pid map
  
4. Generate the Graph by reading each person XML content from https://dblp.org/pid/<pid>.xml


References:
  
  https://dblp.uni-trier.de/xml/
  
  https://dblp.org/faq/1474681.html
  
  https://dblp.org/pid/157/8829.xml
  
  https://www.csauthors.net/distance
