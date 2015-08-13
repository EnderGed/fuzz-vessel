import re

par = []
all_parcelables_with_pkg = set()
pkg_expr = re.compile("package ([^;]*);")
class_expr = re.compile("class ([^ ]*).*\\n?[ \\t]*implements( .+,)* Parcelable")


with open('all_parcelables', 'r') as file:
    par = file.readlines()

for line in par:
    path, _ = line.split(':')
    with open(path, 'r') as file:
        data = file.read()
        pkg = pkg_expr.search(data).group(1)
        cls_names = [i for i, _ in class_expr.findall(data)]
        for cls_name in cls_names:
            all_parcelables_with_pkg.add(pkg + '.' + cls_name)
        
for i in all_parcelables_with_pkg:
    print i
        