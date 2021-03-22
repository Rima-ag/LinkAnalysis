cin = open('./out.munmun_twitter_social', 'r')
cout = open('./in', 'w')

current = "1"
list_ = []
for line in cin:
    line = line.rstrip("\n")
    elem = [string for string in line.split(" ") if string != ""]
    if elem[0] == current:
        list_.append(elem)
    else:
        proba = str(1 / len(list_))
        for x in list_:
            x.append(proba)
            cout.write(x[0] + " " + x[1] + " " + x[2] + "\n")
        list_ = list()
        list_.append(elem)
        current = elem[0]
if len(list_) != 0:
    proba = str(1 / len(list_))
    for x in list_:
        x.append(proba)
        cout.write(x[0] + " " + x[1] + " " + x[2] + "\n")
cin.close()
cout.close()
