'''
Created on Jun 12, 2020

@author: meike
'''


def prepareForJavaCode(data, headersToFormAGroup):

    numberOfGroups = 1
    for header in headersToFormAGroup:
        numberOfGroups = numberOfGroups * len(data[header].unique())

    result = data.filter(headersToFormAGroup, axis=1)
    result["score"] = data["score"]
    result["group"] = 0

    def setGroupID(x, groupId):
        x["group"] = groupId
        return x

    grouped = result.groupby(list(headersToFormAGroup), as_index=False, sort=False)
    groupID = 1
    docString = str(headersToFormAGroup) + "\n"
    for name, group in grouped:
        index = group.index
        if (type(name) is int and name == 0) or (type(name) is tuple and name.count(0) == len(name)):
            # we have found the non-protected group
            result.loc[index, "group"] = 0
            docString = docString + str(name) + " --> 0\n"
        else:
            result.loc[index, "group"] = groupID
            docString = docString + str(name) + " --> " + str(groupID) + "\n"
            groupID += 1

    result = result.drop(columns=headersToFormAGroup)
    print(result["group"].value_counts(normalize=True))
    docString = docString + "\n\n" + str(result["group"].value_counts(normalize=True))
    return result, docString
