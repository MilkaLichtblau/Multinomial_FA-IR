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
    for name, group in grouped:
        index = group.index
        if (type(name) is int and name == 0) or (type(name) is tuple and name.count(0) == len(name)):
            # we have found the non-protected group
            result.loc[index, "group"] = 0
        else:
            result.loc[index, "group"] = groupID
            groupID += 1

    result = result.drop(columns=headersToFormAGroup)
    return result
