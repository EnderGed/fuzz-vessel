#Read the class file and check if it extends or implements any other class or interface.
#If so, store the child-parent pair.

import re, sys, os, sqlite3

path = os.environ['ANDROID_SRC_PATH'] + "/frameworks/"

package_exp = re.compile("(package\s)([A-Za-z_][\.\w]*)(;)")
main_class_exp = re.compile("([A-Za-z_][\w]*)(\.java)")

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
  extends_exp = re.compile("(class " + main_class + " )(extends|implements)( )([A-Za-z_][A-Za-z0-9_]*)(\s?\{)")
  ext = extends_exp.search(content);
  if ext:
    #print full_class_name + " (= " + search_import(ext.group(4), main_class, full_class_name, content)
    c.execute("INSERT INTO Extends(child_class_name, parent_class_name) VALUES ('" + sanitize(full_class_name) + "', '" + sanitize(search_import(ext.group(4), main_class, full_class_name, content)) + "');");
    conn.commit()
  else:
    print "No extensions."
else:
  print "Fail, could not extract class and package name from the file."
conn.close()