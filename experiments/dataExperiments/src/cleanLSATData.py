'''
Created on May 28, 2018

@author: mzehlike

protected attributes: sex, race
features: Law School Admission Test (LSAT), grade point average (UGPA)

scores: first year average grade (ZFYA)

excluding for now: region-first, sander-index, first_pf


höchste ID: 27476

'''
import pandas as pd
from util import prepareForJavaCode


def main():
    data = pd.read_csv("../data/LSAT/law_data.csv")
    data = data.drop(columns=['region_first', 'sander_index', 'first_pf', 'UGPA', 'ZFYA'])

    data['sex'] = data['sex'].replace([2], 0)

    data['LSAT'] = data['LSAT'].apply(str)
    data['LSAT'] = data['LSAT'].str.replace('.7', '.75', regex=False)
    data['LSAT'] = data['LSAT'].str.replace('.3', '.25', regex=False)
    data['LSAT'] = pd.to_numeric(data['LSAT'])
    data = data.rename(columns={"LSAT": "score"})

    data['race'] = data['race'].replace(to_replace="White", value=0)
    data['race'] = data['race'].replace(to_replace="Amerindian", value=1)
    data['race'] = data['race'].replace(to_replace="Asian", value=1)
    data['race'] = data['race'].replace(to_replace="Black", value=1)
    data['race'] = data['race'].replace(to_replace="Hispanic", value=1)
    data['race'] = data['race'].replace(to_replace="Mexican", value=1)
    data['race'] = data['race'].replace(to_replace="Other", value=1)
    data['race'] = data['race'].replace(to_replace="Puertorican", value=1)

    resultData, groups, docString = prepareForJavaCode(data, ["sex", "race"])
    resultData.to_csv("../data/LSAT/LSAT_sexRace_java.csv", header=True, index=False)
    # we need the groups as extra dataframe for the baseline
    groups.to_csv("../data/LSAT/LSAT_sexRace_groups.csv", header=True, index=False)
    with open("../data/LSAT/LSAT_sexRace_doc.txt", "w") as text_file:
        text_file.write(docString)


if __name__ == '__main__':
    main()
