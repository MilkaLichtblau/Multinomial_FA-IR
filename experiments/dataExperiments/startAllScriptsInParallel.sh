#! /bin/bash
module load gcc
module load gnu-parallel
module load bwa
module load samtools

process-dir() {
  # Process all jobs for current folder, sequentially.
  # Input: Folder, e.g. THAKID0001_dir
	'compasAge-p1=01,p2=06.sh'
	'compasAge-p1=02,p2=05.sh'
	'compasAge-p1=03,p2=04.sh'
  'compasAge-p1=04,p2=03.sh'
	'compasAge-p1=05,p2=02.sh'
  'compasAge-p1=06,p2=01.sh'
   compasAge-baseline.sh       
   compasAge-pAllEqual.sh    
   compasAge-pStatisticalParity.sh
   compasRace-baseline.sh  
   compasRace.sh
   compasWorstThree-baseline.sh 
   compasWorstThree-pAllEqual.sh
   compasWorstThree-pStatisticalParity.sh     
   LSATSexRace-baseline.sh
   LSATSexRace-pAllEqual.sh
   LSATSexRace-pStatisticalParity.sh
   germanCreditSexAge-baseline.sh
   germanCreditSexAge-pAllEqual.sh
   germanCreditSexAge-pInbetween.sh  
   germanCreditSexAge-pStatisticalParity.sh
}

export -f process-dir

parallel -j 48 process-dir

