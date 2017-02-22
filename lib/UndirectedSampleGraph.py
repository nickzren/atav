"""
Implements a graph which may be used to aid in removing samples with the
    most familial relations to other samples in a cohort
Written by Brett Copeland <bc2675@cumc.columbia.edu>
"""
from collections import Counter, defaultdict
from random import choice

class UndirectedSampleGraph(object):
    """defines a graph class to maintain information of samples' connections
    based on relatedness
    """
    def __init__(self, desc=None, reciprocal=True):
        # the number of connections for all samples
        self.num_connections = Counter()
        # the actual edges
        self.connections = defaultdict(set)
        # a description of the graph, e.g. affecteds related
        self.desc = desc
        # add a connection from the second sample to the first as well
        self.reciprocal = reciprocal

    def add_edge(self, sample_one, sample_two):
        """add an edge between two samples
        """
        if sample_two not in self.connections[sample_one]:
            self.connections[sample_one].add(sample_two)
            self.num_connections[sample_one] += 1
            if self.reciprocal:
                self.connections[sample_two].add(sample_one)
                self.num_connections[sample_two] += 1

    def empty(self):
        """return whether the graph is empty or not
        """
        return not bool(self.num_connections)

    def remove_sample(self, sample):
        """remove a sample from the graph and all its edges
        """
        if sample in self.connections:
            if self.reciprocal:
                for connected_sample in self.connections[sample]:
                    self.connections[connected_sample].remove(sample)
                    self.num_connections[connected_sample] -= 1
                    if not self.num_connections[connected_sample]:
                        # connected_sample is no longer connected
                        # to the graph either
                        del self.num_connections[connected_sample]
                        del self.connections[connected_sample]
            del self.connections[sample]
            del self.num_connections[sample]

    def get_most_frequent_sample(self):
        """return the sample with the most connections
        """
        return self.num_connections.most_common(1)[0][0]

    def get_all_most_frequent_samples(self):
        """return all samples that have the most connections
        """
        connections = self.num_connections[self.get_most_frequent_sample()]
        samples = []
        for sample, count in self.num_connections.most_common():
            if count == connections:
                samples.append(sample)
            else:
                break
        return samples

    def get_most_frequent_samples_from_list(self, sample_list):
        """return a list of the samples from sample_list that have the most
        connections
        """
        s = defaultdict(list)
        current_max = -1
        for sample in sample_list:
            num_connections = (self.num_connections[sample] if sample
                               in self.num_connections else 0)
            if num_connections >= current_max:
                s[num_connections].append(sample)
                current_max = num_connections
        return s[current_max]

    def get_total_samples(self):
        return (len(self.connections) if self.reciprocal else
                len(set.union(set(self.connections.keys()),
                              *[value for value in self.connections.values()])))

    def get_total_edges(self):
        total_connections = sum(self.num_connections.values())
        if self.reciprocal:
            return total_connections / 2
        else:
            return total_connections

    @staticmethod
    def select_random_sample(sample_list):
        """select a random sample (for deletion) from the specified list
        """
        return choice(sample_list)
