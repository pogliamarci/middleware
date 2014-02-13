for f in *.txt;
do
    cat $f | grep elapsed | cut -d ' ' -f 3 | sed s/elapsed//g | sed s/:/./g | awk -F. '{print ($1 * 6000) + ($2 * 100) + $3 }' > ${f}.parsed
done
