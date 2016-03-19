package Misc;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import SVM.Value;
import Utility.Ranking;

class Gene {
	double[] w = new double[3];
	double f1score;

	Gene() {
		f1score = 0.0;
		w[0] = w[2] = w[3] = 1.0;
	}

	Gene(double _w1, double _w2, double _w3) {
		w[0] = _w1;
		w[1] = _w2;
		w[2] = _w3;
	}
}

class GeneComparator implements Comparator<Gene> {
	@Override
	public int compare(Gene o1, Gene o2) {
		if (o1.f1score == o2.f1score)
			return 0;
		else if (o1.f1score < o2.f1score)
			return 1;
		else
			return -1;
	}
}

public class GeneticAlgorithm {
	double[][] mx1;
	double[][] mx2;
	double[][] mx3;
	ArrayList<Hashtable<Integer, Boolean>> truth;
	
	// For GA
	private int bestNGenes = 5;
	private double rangeMin = 0;
	private double rangeMax = 1000;
	private int maxGenerations = 1000000;
	private double mutationChance = 0.25;


	public GeneticAlgorithm(double[][] _mx1, double[][] _mx2, double[][] _mx3, ArrayList<Hashtable<Integer, Boolean>> _truth) {
		mx1 = _mx1;
		mx2 = _mx2;
		mx3 = _mx3;
		truth = _truth;
	}
	
	public Value getScore(double l1, double l2, double l3, int k) {
		double[][] mx = new double[mx1.length][mx1[0].length];
		for (int i = 0; i < mx.length; i++) {
			for (int j = 0; j < mx[0].length; j++) {
				mx[i][j] = (l1 * mx1[i][j] +l2 *  mx2[i][j] +l3 *  mx3[i][j]) / 3.0;
			}
		}
		double sum_sk = 0.0;
		double sum_pk = 0.0;
		double sum_rk = 0.0;
		double acc_sk = 0.0;
		double acc_pk = 0.0;
		double acc_rk = 0.0;
		Ranking rk = new Ranking();
		for (int n = 0; n < mx.length; n++) {
			Hashtable<Integer, Boolean> interest = truth.get(n);
			double curr_rk = 0;
			// find the number of correct interest;
			for(int i=0;i<GlobalHelper.numClasses;i++) {
				if (interest.containsKey(i)) {
					curr_rk += 1.0;
				}
			}
			sum_rk += curr_rk;
			sum_sk += 1.0;
			sum_pk += k;
			double[] row = mx[n];
			int[] list = rk.rank(row);
			
			boolean flag = false;
			for (int i = 0; i < k; i++) {
				if (interest.containsKey(list[i])) {
					acc_pk += 1.0;
					flag = true;
				}
			}
			if (flag) {
				acc_sk += 1.0;
			}
		}
		Value v = new Value();
		v.pk = acc_pk / sum_pk;
		v.sk = acc_sk / sum_sk;
		v.rk = acc_rk / sum_rk;
		return v;
	}

	private double runTestGA(Gene gene) {
		double precision = 0;
		for(int k=1;k<=10;k++) {
			Value v = getScore(gene.w[0],gene.w[1],gene.w[2],k);
			precision += v.pk;
		}
		precision = precision/10.0;
		return precision;
	}

	public void generateRandomGenes(ArrayList<Gene> geneList) {
		Random random = new Random();
		for (int j = 0; j < bestNGenes; j++) {
			double[] value = new double[3];
			for (int i = 0; i < 3; i++) {
				value[i] = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
			}
			Gene gene = new Gene(value[0], value[1], value[2]);
			geneList.add(gene);
		}
	}

	private Gene generateOffspring(Gene gene1, Gene gene2) {
		Random randomCrossover = new Random();
		Random randomMutation = new Random();
		Random randomPositiveMutation = new Random();
		Random randomValue = new Random();
		double[] value = new double[3];
		for (int i = 0; i < 3; i++) {

			// Crossover
			if (randomCrossover.nextDouble() < 0.5) {
				value[i] = gene1.w[i];
			} else {
				value[i] = gene2.w[i];
			}

			// Mutation
			double randMutation = randomMutation.nextDouble();
			double randPositive = randomPositiveMutation.nextDouble();
			double randValue = randomValue.nextDouble();
			if (randMutation < mutationChance / 4.0) {
				if (randPositive < 0.5) {
					value[i] = value[i] + (randValue * 100) + 1;
				} else {
					value[i] = value[i] - (randValue * 100) + 1;
				}
			} else if (randMutation < mutationChance / 2.0) {
				if (randPositive < 0.5) {
					value[i] = value[i] + (randValue * 10) + 1;
				} else {
					value[i] = value[i] - (randValue * 10) + 1;
				}
			} else if (randMutation < mutationChance) {
				if (randPositive < 0.5) {
					value[i] = value[i] + (randValue * 2);
				} else {
					value[i] = value[i] - (randValue * 2);
				}
			}
			// Make sure attributes have proper signs
			if (((i < 6) || (i == 11)) && value[i] < 0) {
				value[i] = -1 * value[i];
			} else if (((i >= 6) && (i <= 10)) && value[i] > 0) {
				value[i] = -1 * value[i];
			}
		}
		Gene offspring = new Gene(value[0], value[1], value[2]);
		return offspring;
	}

	public void generateNewGenes(ArrayList<Gene> geneList) {
		ArrayList<Gene> tempGeneList = new ArrayList<Gene>();
		for (Gene gene : geneList) {
			tempGeneList.add(gene);
		}
		geneList.clear();

		for (int i = 0; i < bestNGenes - 1; i++) {
			for (int j = i + 1; j < bestNGenes; j++) {
				Gene offSpring = generateOffspring(tempGeneList.get(i), tempGeneList.get(j));
				geneList.add(offSpring);
			}
		}
		for (int i = 0; i < bestNGenes; i++) {
			Gene fittestGene = tempGeneList.get(i);
			geneList.add(fittestGene);
		}
		tempGeneList.clear();
	}

	public void runGA() {
		ArrayList<Gene> geneList = new ArrayList<Gene>();

		generateRandomGenes(geneList);
		for (int i = 0; i < maxGenerations; i++) {
			System.out.println("--------- Generation " + i + " ----------");
			System.err.println("--------- Generation " + i + " ----------");
			generateNewGenes(geneList);
			generateRandomGenes(geneList);
			for (Gene currGene : geneList) {
				currGene.f1score = runTestGA(currGene);
				System.out.println(currGene.w[0] + ", " + currGene.w[1] + ", " + currGene.w[2]);
				System.out.println("Mean f1-Score : " + currGene.f1score);

			}
			geneList.sort(new GeneComparator());
			for (int z = 0; z < bestNGenes; z++) {
				Gene currGene = geneList.get(z);
				System.err.println(currGene.w[0] + ", " + currGene.w[1] + ", " + currGene.w[2]);
				System.err.println("Mean f1-Score : " + currGene.f1score);
			}
		}
	}
}