#Read a class file.
#Find a writeToParcel method.
#Check the names of all writeToParcel elements which were used inside it.
#Find the elements declarations / inits in the file.
#Return type / name / declatation triples.

import re, sys, os, sqlite3
#I assume that no psychopath would insert comments inside the class name delaration
write_to_parcel_exp = re.compile("public[\s]*void[\s]*writeToParcel\(.*\)[\s]*\{[\s\S]*",)
single_write_exp = re.compile("dest\.write[A-Za-z0-9_]*\(.*\);")
native_write_exp = re.compile("nativeWriteToParcel\(.*\);")
write_recursive_exp = re.compile("^(log\(\")[\S]*.writeToParcel\(.*\);");
parcel_var_exp = re.compile("\([\s]*Parcel[\s]*[\S]*")
parcel_var_narrow_exp = re.compile("[^(Parcel )][A-Za-z0-9]+")
field_exp = re.compile("(public|private).*;")
package_exp = re.compile("(package\s)([A-Za-z_][\.\w]*)(;)")
main_class_exp = re.compile("([A-Za-z_][\w]*)(\.java)")

path = os.environ['ANDROID_SRC_PATH'] + "/frameworks/"

keywords_types = { "if", "else", "for", "while", "do", "int", "float", "double", "boolean", "throw", "new", "Parcel", "super", "null", "this"}

def getUnnestedContents(content):
  index = 0
  inside_class = False
  while index<len(content):
    if content[index] == "{":
      if not inside_class:
	inside_class = True
	content = content[index:len(content)-1]
	index = 0
      else:
	content = content[:index] + content[reap_out_index(index, content):]
    elif index+1<len(content) and content[index:index+2]=="/*":
      content = content[:index] + content[reap_out_block_comment_index(index, content):]
    elif index+1<len(content) and content[index:index+2]=="//":
      content = content[:index] + content[reap_out_single_line_comment_index(index, content):]
    index += 1
  return content
  
def search_import(classname, main_class, full_class_name, content):
  primitives = [ "boolean", "byte", "char", "double", "float", "int", "long", "short" ]
  if classname in primitives:
    return classname
  if classname == main_class:
    return full_class_name
  import_exp = re.compile("(import\s)([A-Za-z_][\.\w]*" + classname + ")(;)")
  result = import_exp.search(content)
  if result:
    return result.group(2)
  return "java.lang." + classname
  
def remove_rubbish(content):
  index = 0
  while index<len(content):
    if index+1<len(content) and content[index:index+2]=="/*":
      content = content[:index] + content[reap_out_block_comment_index(index, content):]
    elif index+1<len(content) and content[index:index+2]=="//":
      content = content[:index] + content[reap_out_single_line_comment_index(index, content):]
    elif content[index]=="\"":
      content = content[:index] + content[reap_out_string_index(index, content, False):]
    elif content[index]=="\'":
      content = content[:index] + content[reap_out_string_index(index, content, True):]
    index += 1
  return content  
  
def reap_out_block_comment_index(index, message):
  length = len(message)
  index += 2
  while index+1<len(content) and message[index:index+2]!="*/":
    index += 1
  return index + 2
  
def reap_out_single_line_comment_index(index, message):
  length = len(message)
  index += 2
  while index<length and message[index]!='\r' and message[index]!='\n':
    index += 1
  return index + 1
  
def reap_out_string_index(index, message, isChar):
  index += 1
  length = len(message)
  backslashes = 1
  mark = '\'' if isChar else '\"'
  while index<length and backslashes % 2 == 1:
    if message[index] == mark:
      backslashes = 0
      back_index = index - 1
      while message[back_index]=="\\":
	backslashes += 1
    index += 1
  return index
  
  
def reap_out_index(index, message):
  length = len(message)
  stack_size = 1
  index += 1
  while stack_size>0:
    if index>=length:
      print "Error: OOB."
      return None
    if message[index]=='{':
      stack_size += 1
    elif message[index]=='}':
      stack_size -= 1
    elif index+1<length and message[index:index+2]=="//":
      index += 2
      while index<length and message[index]!='\r' and message[index]!='\n':
	index += 1
    elif index+1<length and message[index:index+2]=="/*":
      index += 2
      while index+1<length and message[index:index+2]!="*/":
	index += 1
      index += 1
    index += 1
  return index

def reap_intestines(message):
  index = reap_out_index(0, message)
  return message[0:index+1]
  
def get_field_name_candidates(mes_start, message):
  parcel_var = parcel_var_exp.search(mes_start)
  if parcel_var == None:
    print "Error: Can't find parcel var."
    return None
  parcel_var = parcel_var_narrow_exp.search(parcel_var.group(0))
  if parcel_var != None:
    parcel_var = parcel_var.group(0)
  not_parcel_var_exp = re.compile("[A-Za-z_][A-Za-z0-9_]*[\(]?")
  found = not_parcel_var_exp.findall(message)
  candidates = []
  if found:
    for can in found:
      if not can.endswith('('):
	if can not in keywords_types and not (parcel_var != None and can.startswith(parcel_var)) and can not in candidates:
	  candidates.append(can)
    return candidates
  else:
   return None
   
def def_in_line(line, name):
  #name_single_exp = re.compile("[^A-Za-z0-9_]" + name + "[^A-Za-z0-9_(]")
  name_single_exp = re.compile("[^A-Za-z0-9_]" + name + ";")
  result = name_single_exp.search(line)
  if result != None:
    split = line.split(" ")
    for i in range(len(split)):
      if i>0 and split[i].startswith(name):
	type = split[i-1]
	return [type, name]
  return None
  
def sanitize(name):
  sane_exp = re.compile("[)(;\"\']");
  return  sane_exp.sub('', (name.replace('<', '\\lt')).replace('>', '\\gt'))
  
  
all_stf = set()
content = open(path + sys.argv[1], 'r').read()
package = package_exp.search(content)
main_class = main_class_exp.search(sys.argv[1])
if package and main_class:
  package = package.group(2)
  main_class = main_class.group(1)
  full_class_name = package + '.' + main_class
  
  method = write_to_parcel_exp.search(content)
  if method:
    method = method.group(0)
    method_start = method[:method.find("{")]
    method_body = remove_rubbish(reap_intestines(method[method.find("{"):]))
    #print method_body
    candidates = get_field_name_candidates(method_start, method_body)
        
    if candidates and len(candidates)!=0:
      declarations = getUnnestedContents(content)
      '''print "=== " + sys.argv[1] + " ==="
      for c in candidates:
	for line in declarations.splitlines():
	  pair = def_in_line(line, c)
	  if pair:
	    print pair'''	  
      conn = sqlite3.connect('vessel_objects.db')
      c = conn.cursor()
      for can in candidates:
	for line in declarations.splitlines():
	  pair = def_in_line(line, can)
	  if pair:
	    c.execute("INSERT INTO Fields(name, declaring_class_name, type_class_name) VALUES ('" + sanitize(pair[1]) + "', '" + sanitize(full_class_name) + "', '" + sanitize(search_import(pair[0], main_class, full_class_name, content)) + "');")
      conn.commit()
      conn.close()