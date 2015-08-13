

with open('services', 'r') as f:
    data = f.readlines()
    
servs = []
this_serv = []
for line in data:
    if line.startswith('private') or line.startswith('public'):
        servs.append(this_serv)
        this_serv = []
    this_serv.append(line)

servs = [serv for serv in servs[1:] if 'SERVICE' in serv[0]]

serv_names = [serv[0].split()[-1] for serv in servs]
mana_names = [serv[3].split()[6] if serv[3].split()[0] == 'Use' else serv[3].split()[0] for serv in servs]
str_values = [serv[-1].split()[-1][1:-1] for serv in servs]

services = zip(serv_names, mana_names, str_values)

