'''
Created on Sep 28, 2020

@author: meike
'''
import unittest
import pandas as pd
from src.util import averageGroupExposureGain, positionBias


class Test(unittest.TestCase):

    def testPositionBias(self):
        ranking = pd.DataFrame.from_dict({'group':[0, 0, 0, 1, 1, 1, 2, 2, 2],
                                          'score':[9, 8, 7, 6, 5, 4, 3, 2, 1]})
        # total position bias
        expected = (1 + 0.63092975357 + 0.5 + 0.43067655807 + 0.38685280723 + 0.3562071871 + 0.33333333333 + 0.31546487678 + 0.30102999566)
        actual = positionBias(ranking)

        self.assertAlmostEqual(expected, actual, places=10)

        # group 1 position bias
        groupOneRanking = ranking.loc[ranking["group"] == 1]
        expected = (0.43067655807 + 0.38685280723 + 0.3562071871)
        actual = positionBias(groupOneRanking)

        self.assertAlmostEqual(expected, actual, places=10)

        # group 2 position bias
        groupTwoRanking = ranking.loc[ranking["group"] == 2]
        expected = (0.33333333333 + 0.31546487678 + 0.30102999566)
        actual = positionBias(groupTwoRanking)

        self.assertAlmostEqual(expected, actual, places=10)

    def testAverageGroupExposureGain(self):
        colorblindRanking = pd.DataFrame.from_dict({'group':[0, 0, 0, 1, 1, 1, 2, 2, 2],
                                                     'score':[9, 8, 7, 6, 5, 4, 3, 2, 1]})
        fairRanking = pd.DataFrame.from_dict({'group':[0, 1, 2, 0, 1, 2, 0, 1, 2],
                                               'score':[9, 6, 3, 8, 5, 2, 7, 4, 1]})
        actual = pd.DataFrame.from_dict({'group':[0, 1, 2]})
        actual = averageGroupExposureGain(colorblindRanking, fairRanking, actual)

        # gains have to be normalized by group size because otherwise numbers for small groups are not comparable
        groupZeroGain = (1 + 0.43067655807 + 0.33333333333) - (1 + 0.63092975357 + 0.5)
        groupOneGain = (0.63092975357 + 0.38685280723 + 0.31546487678) - (0.43067655807 + 0.38685280723 + 0.3562071871)
        groupTwoGain = (0.5 + 0.3562071871 + 0.30102999566) - (0.33333333333 + 0.31546487678 + 0.30102999566)
        expected = pd.DataFrame.from_dict({'group':[0, 1, 2],
                                           'expGain':[groupZeroGain, groupOneGain, groupTwoGain]})

        pd.testing.assert_frame_equal(expected, actual)


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testAverageGroupExposureGain']
    unittest.main()
