#! /bin/bash
module load gcc
module load gnu-parallel
module load bwa
module load samtools

  # Process all jobs for current folder, sequentially.
parallel -j 48 < 'compasAge-p1=01,p2=06.sh'
parallel -j 48 < 'compasAge-p1=02,p2=05.sh'
parallel -j 48 < 'compasAge-p1=03,p2=04.sh'
parallel -j 48 < 'compasAge-p1=04,p2=03.sh'
parallel -j 48 < 'compasAge-p1=05,p2=02.sh'
parallel -j 48 < 'compasAge-p1=06,p2=01.sh'
parallel -j 48 < compasAge-baseline.sh       
parallel -j 48 < compasAge-pAllEqual.sh    
parallel -j 48 < compasAge-pStatisticalParity.sh
parallel -j 48 < compasRace-baseline.sh  
parallel -j 48 < compasRace.sh
parallel -j 48 < compasWorstThree-baseline.sh 
parallel -j 48 < compasWorstThree-pAllEqual.sh
parallel -j 48 < compasWorstThree-pStatisticalParity.sh     
parallel -j 48 < LSATSexRace-baseline.sh
parallel -j 48 < LSATSexRace-pAllEqual.sh
parallel -j 48 < LSATSexRace-pStatisticalParity.sh
parallel -j 48 < germanCreditSexAge-baseline.sh
parallel -j 48 < germanCreditSexAge-pAllEqual.sh
parallel -j 48 < germanCreditSexAge-pInbetween.sh  
parallel -j 48 < germanCreditSexAge-pStatisticalParity.sh



