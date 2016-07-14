#!/usr/bin/python -tt
# Copyright 2010 Google Inc.
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

# Google's Python Class
# http://code.google.com/edu/languages/google-python-class/

import sys
import re
import csv
import os
import filecmp
import multiprocessing
import traceback



def get_file_paths(directory):
    file_paths = []

    for root, directories, files in os.walk(directory):
        for filename in files:
            match = re.search(r'.csv$', filename)
            if match:
                filepath = os.path.join(root, filename)
                file_paths.append(filepath)

    return file_paths


def cmp_output_files(new_file_paths, old_file_paths):
    # print(new_file_paths)
    # print(old_file_paths)

    new_files_set = set(map(lambda s: s[len(new_folder):],new_file_paths))
    old_files_set = set(map(lambda s: s[len(old_folder):],old_file_paths))


    # first, compare file sets
    diff_new_old = list(new_files_set - old_files_set)
    diff_old_new = list(old_files_set - new_files_set)
    print "\033[31m"
    if len(diff_new_old) > 0:
        print "Warning: Following NEW files added"
        for f in diff_new_old:
            print "\t"+f
    if len(diff_old_new) > 0:
        print "\nWarning: Following OLD files deleted:"
        for f in diff_old_new:
            print "\t"+f
    print "\033[m"

    # then, compare file contents
    file_paths = list(new_files_set & old_files_set) # intersection
    wpool = multiprocessing.Pool(4)

    # wpool.map(try_cmp_two_files,file_paths)
    for f in file_paths:
        # wpool.apply_async(try_cmp_two_files, args=(f, ), callback=check_result)
        wpool.apply_async(try_cmp_two_files, args=(f, ))
    wpool.close()
    wpool.join()

    # for i, j in enumerate(new_file_paths):
        # cmp_two_files(new_file_paths[i], old_file_paths[i])

    return

def check_result(r):
    print("callback: "+str(r))
    # if r != 0: # if not "OK flag"
    

def try_cmp_two_files(f):
    try:
        # print "Worker "+str(os.getpid())+" comparing " + f
        cmp_two_files(f)
    except SystemExit:
        print('\nAborting ...')
    except:        
        e = sys.exc_info()
        print "\033[31m"+"Worker exception:\n"+str(e)+"\n\033[m"
        traceback.print_tb(e[2])
        # print(e)
        # print "\tFile: %s" % f
        
    

def cmp_two_files(f):
    new_file = new_folder+f
    old_file = old_folder+f
    # print "comparing\n"+"\t"+new_file+"\n\t"+old_file
    if filecmp.cmp(new_file, old_file):
        print "File "+f+" OK!"
        return

    new_headers = get_header(new_file)
    old_headers = get_header(old_file)

    new_headers_index = get_new_headers_index_list(new_headers, old_headers)

    new_data_list = get_data_list(new_file)
    old_data_list = get_data_list(old_file)

    for index, item in enumerate(old_data_list):
        for old, new in enumerate(new_headers_index):
            try:
                if new_headers[new] == old_headers[old]:
                    if new >= len(new_data_list[index]):
                        if old >= len(item) or item[old].strip() == '':
                            continue
                        else:
                            print "\033[31m"+"--> Missing value for file "+f+" at:"
                            print "row: "+str(index)+" / column:"+new_headers[new]
                            print "\033[m"
                    if item[old] != new_data_list[index][new]:
                        print "\033[31m"+"--> File "+f+" differs at:"
                        print new_file, new_headers[new], item[0], index, item[old], new_data_list[index][new]
                        print "\033[m"
            except:
                e = sys.exc_info()
                print "\033[31m"+"\nException while comparing rows:"
                print str(e[0])+' '+str(e[1])
                traceback.print_tb(e[2])
                print('\033[m')
                print('\nDebug info:')
                print('Filename: '+f)
                print('Old headers not present in new file : '+str(list(set(old_headers) - set(new_headers))))
                print('New headers not present in old file : '+str(list(set(new_headers) - set(old_headers))))
                print('Row index: '+str(index))
                print('Old/new column names: '+str(old_headers[old])+' / '+str(new_headers[new]))
                exit(-1)

    print "File "+f+" OK!"
    return


def get_new_headers_index_list(new_headers, old_headers):
    new_headers_index = []

    for i, j in enumerate(new_headers):
        if j in old_headers:
            new_headers_index.append(i)

    return new_headers_index


def get_header(file):
    # print file
    f = open(file)
    csv_f = csv.reader(f)
    headers = next(csv_f, None)

    return headers


def get_data_list(file):
    list = []
    f = open(file)
    csv_f = csv.reader(f)

    for row in csv_f:
        list.append(row)

    return list

new_folder=sys.argv[1]
old_folder=sys.argv[2]

def main():
    # This basic command line argument parsing code is provided.
    # Add code to call your functions below.

    # Make a list of command line arguments, omitting the [0] element
    # which is the script itself.

    args = sys.argv[1:]

    new_file_paths = []
    new_file_paths = get_file_paths(args[0])

    old_file_paths = []
    old_file_paths = get_file_paths(args[1])

    cmp_output_files(new_file_paths, old_file_paths)
    """

    new_file = args[0]
    old_file = args[1]

    cmp_two_files(new_file, old_file)
"""
    # print '\n'.join(paths)

if __name__ == "__main__":
    main()