#!/usr/bin/env python
import sys
import os
import logging

## This is a script to convert dpbin mysql table dump from 10k -> 1k block size
## Should be possible to change the new block size to any proper divisors of 10000
## i.e. 1, 500, 1000, 2000, 2500, 5000, etc. in the collapse_bins() function
## Only tested with 1000 though

## Define some globals storing bin value info 
current_bin_vals = {'a':0,'b':3,'c':10,'d':20,'e':30,'f':50,'g':60,'h':80,
                    'i':100,'j':150,'k':200,'l':250,'m':300,'n':400,'o':500,'p':600,'q':800,'r':1000}
new_bins = ['a','b','c','d','e','f','g']

def return_new_bin(val):
    """
    return new bin value 
    """    
    if val < 3:
        return 'a'
    elif val < 10:
        return 'b'
    elif val < 20:
        return 'c'
    elif val < 30:
        return 'd' 
    elif val < 50:
        return 'e'
    elif val < 200:
        return 'f'
    else:
        return 'g'

def expand_bins(bin_string):
    """
    bin_string : a base36 encoded(for bin lengths) dp string 
    """
    char_bin = ''
    expanded_string = ''
    for i in range(0,len(bin_string)):
        if bin_string[i] in current_bin_vals:
            bin_length = convert_to_int(char_bin)
            bin_val = bin_string[i]
            new_bin_val = return_new_bin(current_bin_vals[bin_val])
            expanded_string+=new_bin_val*bin_length
            char_bin = ''
        else:
            char_bin+=bin_string[i]

    return expanded_string

def collapse_bins(expanded_bin_string, new_block_size, block_id, sample_id):
    """ Collapse an expanded bin string into individal blocks of a new size 

    expanded_bin_string : The original 10k bin_string with expanded bin lengths
    i.e. each base has a bin value
    
    new_block_size : The new block size to convert to 

    block_id : The original 10K block id for this bin string
    
    sample_id : The sample id for this bin string   
    """
    out_bin = ''
    total_bin_length = 0
    prev_bin = expanded_bin_string[0]
    counter = 0
    bin_counter = 0
    new_block_id = int(block_id) * 10 
    exclusion_str = radix36_encoding(new_block_size)+'a'
    n = len(expanded_bin_string)
    
    for i in range(0,n):
        if bin_counter == new_block_size:
            out_bin+=radix36_encoding(counter)+prev_bin
            if out_bin != exclusion_str:
                print sample_id+'\t'+str(new_block_id)+'\t'+out_bin
            out_bin = ''
            total_bin_length += counter 
            counter = 0
            bin_counter = 0
            new_block_id += 1 
            
        if expanded_bin_string[i] != prev_bin:
            out_bin+=radix36_encoding(counter)+prev_bin
            total_bin_length += counter
            counter = 0

        counter+=1
        prev_bin = expanded_bin_string[i]
        bin_counter+=1
        
    out_bin+=radix36_encoding(counter)+prev_bin
    total_bin_length += counter
    if total_bin_length != 10000:
        logging.info(block_id+'\t'+sample_id+'\tfailed the conversion process,still present in output ')
    if out_bin != exclusion_str:
        print sample_id+'\t'+str(new_block_id)+'\t'+out_bin    
    
def return_bins(chrom):
    """ Query db1 and return dp bins 
    chrom : chromosome to query
    """    
    db = MySQLdb.connect(
        read_default_file="/home/rp2801/.my.cnf",
        read_default_group="clientdb1",database="WalDB")
    cur = db.cursor()
    cur.execute("SELECT * FROM DP_bins_%s limit 10")
    while True:
        res = cur.fetchone()
        yield res[0][0]
    
def convert_to_int(bin_len):
    """ convert base36 back to int
    """
    return int(bin_len,36)

def radix36_encoding(number):    
    """ returns base36 encoded value for a given decimal number
    Refer to : http://stackoverflow.com/questions/1181919/python-base-36-encoding
    number => numerical coverage value in decimal
    """
    alphabet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'

    if not isinstance(number, (int, long)):
        raise TypeError('number must be an integer')

    base36 = ''
    sign = ''

    if number < 0:
        sign = '-'
        number = -number
        raise Exception("Negative bin length encoutered, bug in your code !")

    if 0 <= number < len(alphabet):
        return sign + alphabet[number]

    while number != 0:
        number,i = divmod(number,len(alphabet))
        base36 = alphabet[i] + base36

    return str(sign + base36)

def read_file(bin_dump):
    """
    bin_dump : str ; path to the dp bin file 
    """
    with open(bin_dump,"r") as IN:
        for line in IN:
            sample_id,block_id,block_str = line.strip('\n').split('\t')
            expanded_string = expand_bins(block_str)
            if len(expanded_string) != 10000:
                logging.info(sample_id+'\t'+block_id+'\tfailed_the_expansion(expanded bin string is not 10000 bases will not be in output)')
                continue
            collapse_bins(expanded_string,1000,block_id,sample_id)

def main():
    """ Main Function
    """
    if len(sys.argv) != 3:
        print "RUN as : python/pypy convert_bins.py <bin_dump_file> <log_file> > <converted_bin_file>\n"
        sys.exit(1)
        
    ## Command line arguments 
    bin_dump = sys.argv[1]
    log_file = sys.argv[2]
    ## Set logging
    logging.basicConfig(filename=log_file,level=logging.DEBUG,
                        filemode='w')
    read_file(bin_dump)
        
if __name__ == '__main__':
    main()
