#Read class file. Get package and class name.
#Find the constructor.
#If any: Get constructor arguments.
#TODO: Look for private classes. Repeat process for private classes.

import re, sys, os, sqlite3

path = os.environ['ANDROID_SRC_PATH'] + "/frameworks/"

package_exp = re.compile("(package\s)([A-Za-z_][\.\w]*)(;)")
main_class_exp = re.compile("([A-Za-z_][\w]*)(\.java)")
type_exp = re.compile("([A-Z_][A-Za-z0-9_]*)")

main_class = ''
full_class_name = ''
content = ''

def search_import(match):
  classname = match.group(1)
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
  
def cut_off(string):
  stack = 1
  for i in range(len(string)):
    if i==0:
      continue
    if string[i]=='<':
      stack = stack + 1
    elif string[i]=='>':
      stack = stack - 1
    if stack==0:
      return i + 1
  return -1
  
def parse_angle_brackets(constr):
  acc = []
  index = 0
  while index < len(constr):
    if constr[index]=='<':
      acc.append(constr[:index])
      constr = constr[index:]
      index = cut_off(constr)
      acc.append(constr[:index])
      constr = constr[index:]
      index = 0
    else:
      index = index + 1
  if index > 0:
    acc.append(constr[:index])
  intermediate = []
  for a in acc:
    if a[:1]!='<':
      if a[:1]==' ':
	asplit = a[1:].split(" ")
	for i in range(0, len(asplit)):
	  if i % 2 == 1:
	    intermediate.append(asplit[i])
      else:
	asplit = a.split(" ")
	for i in range(0, len(asplit)):
	  if i % 2 == 0:
	    intermediate.append(asplit[i])
    else:
      intermediate.append(a)
  acc = []
  index = 0
  while index < len(intermediate)-1:
    if intermediate[index+1][:1]=='<':
      acc.append(intermediate[index] + intermediate[index+1])
      index = index + 2
    else:
      acc.append(intermediate[index])
      index = index + 1
  if index<len(intermediate) and intermediate[index][:1]!='<':
    acc.append(intermediate[index])
  return acc
  
def sanitize(name):
  sane_exp = re.compile("[)(;\"\']");
  return  sane_exp.sub('', (name.replace('<', '\\lt')).replace('>', '\\gt'))
      

conn = sqlite3.connect('vessel_objects.db')
c = conn.cursor()
content = open(path + sys.argv[1], 'r').read()
package = package_exp.search(content)
main_class = main_class_exp.search(sys.argv[1])
if package and main_class:
  package = package.group(2)
  main_class = main_class.group(1)
  full_class_name = package + '.' + main_class
  #print full_class_name
  c.execute("INSERT INTO Classes(name) VALUES ('" + sanitize(full_class_name) + "');")
  conn.commit()
  constructor_exp = re.compile("(new)?([\s]+" + main_class + "\()(.*)(\))")
  possible_constructors = constructor_exp.findall(content)
  formatted = []
  if possible_constructors:
    constr_number = 1
    for constructor in possible_constructors:
      if len(constructor[0])==0:
	c.execute("INSERT INTO Constructors(number, class_name) VALUES (" + str(constr_number) + ", '" + sanitize(full_class_name) + "');");
	conn.commit()
	constr_id = c.execute("SELECT id FROM Constructors WHERE class_name = '" + sanitize(full_class_name) + "' AND number = " + str(constr_number) + " LIMIT 1;").fetchone()[0]
	c_params = parse_angle_brackets(constructor[2])
	for i in range(len(c_params)):
	  c_params[i] = type_exp.sub(search_import, c_params[i]);
	  c.execute("INSERT INTO Args(number, class_name, constructor_id) VALUES (" + str(i+1) + ", '" + sanitize(c_params[i]) + "', " + str(constr_id) + ");");
	constr_number = constr_number + 1
	#print c_params
  else:
    print "Null"
  conn.commit()
  conn.close()

else:
  print "Fail, could not extract class and package name from the file."