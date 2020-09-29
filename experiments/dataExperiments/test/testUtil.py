'''
Created on Sep 28, 2020

@author: meike
'''
import unittest
import pandas as pd


class Test(unittest.TestCase):

    def testAverageGroupExposureGain(self):
        colorblind_ranking = pd.Dataframe.from_dict({'group':[0, 0, 0, 1, 1, 1, 2, 2, 2],
                                                     'score':[9, 8, 7, 6, 5, 4, 3, 2, 1]})
        fair_ranking = pd.Dataframe.from_dict({'group':[0, 1, 2, 0, 1, 2, 0, 1, 2],
                                               'score':[9, 6, 3, 8, 5, 2, 7, 4, 1]})


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testAverageGroupExposureGain']
    unittest.main()
