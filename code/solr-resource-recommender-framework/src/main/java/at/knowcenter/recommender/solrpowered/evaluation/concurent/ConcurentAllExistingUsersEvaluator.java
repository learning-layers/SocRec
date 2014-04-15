package at.knowcenter.recommender.solrpowered.evaluation.concurent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import at.knowcenter.recommender.solrpowered.engine.strategy.StrategyType;
import at.knowcenter.recommender.solrpowered.evaluation.RecommenderEvaluator;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.*;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.hybrid.CF_CFcat_C_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.hybrid.UB_InterestsGroups_Social_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.marketplace.MP_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.marketplace.cb.C_Description_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.marketplace.cb.C_Name_Description_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.marketplace.cb.C_Name_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.marketplace.cf.CF_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.UB_CustomerGroups_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.UB_Interests_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.cb.SocialStream_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.cf.CF_Social_Comments_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.cf.CF_Social_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.cf.CF_Social_Likes_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.combined.CFPurchWithSocCommonNeighborhoodRecommender_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.combined.CFPurchWithSocCommonNeighborhoodReplacedRecommender_Job;
import at.knowcenter.recommender.solrpowered.evaluation.concurent.jobs.social.combined.CFPurchWithSocCommonNeighborhoodSummedRecommender_Job;
import at.knowcenter.recommender.solrpowered.evaluation.metrics.MetricsExporter;
import at.knowcenter.recommender.solrpowered.services.SolrServiceContainer;
import at.knowcenter.recommender.solrpowered.services.cleaner.DataFetcher;
import at.knowcenter.recommender.solrpowered.services.impl.actions.RecommendService;
import at.knowcenter.recommender.solrpowered.services.impl.item.ItemService;
import at.knowcenter.recommender.solrpowered.services.impl.user.UserService;

public class ConcurentAllExistingUsersEvaluator {

	public static void main(String... args) {
		ConcurentAllExistingUsersEvaluator evaluator = new ConcurentAllExistingUsersEvaluator();
		evaluator.evaluate();
	}
	
	
	private List<String> users;
	
	public void evaluate() {
		long startTime = System.nanoTime();
		
		initEvaluation();
		
		ExecutorService executor = Executors.newFixedThreadPool(4);
		
		List<Callable<List<MetricsExporter>>> jobs = new ArrayList<Callable<List<MetricsExporter>>>();
		
		int fullUserSize = users.size();
		
		int partitionCount = 150;
		
		for (int i = 0; i < partitionCount; i++) {
			
			int partitionSize = fullUserSize / partitionCount;
			int offset = (i + 1) * partitionSize;
			
			if (i == partitionCount - 1) {
				offset = fullUserSize;
			}
			
			List<String> userPartition= users.subList(i * partitionSize, offset);

//			jobs.add(new CommonStrategyJob(userPartition, "CF_Rev", StrategyType.CF_Review, 0));
//			jobs.add(new CommonStrategyJob(userPartition, "MP", StrategyType.MostPopular_Review, 1));
//			jobs.add(new CommonStrategyJob(userPartition, "Soc", StrategyType.CF_Social, 2));
//			jobs.add(new C_Name_Job(userPartition));
//			jobs.add(new C_Description_Job(userPartition));
//			jobs.add(new CommonStrategyJob(userPartition, "Cat_" + i, StrategyType.CF_Categories, 3));

//			jobs.add(new UB_Interests_Job(userPartition, "interests_" + i));
//			jobs.add(new UB_CustomerGroups_Job(userPartition, "groups_" + i));
//
//			jobs.add(new CommonStrategyJob(userPartition, "Bio_" + i, StrategyType.BiographyBasedMLT, 4));
//			jobs.add(new CommonStrategyJob(userPartition, "Real_Bio" + i, StrategyType.RealBiographyBasedMLT, 5));
//			jobs.add(new SocialStream_Job(userPartition, "soc_stream_job_" + i));
//			jobs.add(new CF_Social_Likes_Job(userPartition, "likes_"+i));
//			jobs.add(new CF_Social_Comments_Job(userPartition, "comments_"+i));
//			jobs.add(new CommonStrategyJob(userPartition, "Wallposts_" + i, StrategyType.WallPostInteraction, 6));
//			jobs.add(new CommonStrategyJob(userPartition, "Snapshots_" + i, StrategyType.SnapshotInteraction, 7));

			jobs.add(new CommonStrategyJob(userPartition, "InterestsC" + i, StrategyType.UB_Interests_MLT, 8));

			
			//			jobs.add(new CF_C_UB_Soc_MP_Job(userPartition, "all_" + i, 0));
			
//			jobs.add(new CF_CFcat_C_Job(userPartition, "all_" + i));
//			jobs.add(new UB_InterestsGroups_Social_Job(userPartition, "all_" + i));
//			
//			jobs.add(new MP_Job(userPartition, "mp_" + i));
//			jobs.add(new CF_Job(userPartition,"job_" + i));
//			jobs.add(new CF_Social_Job(userPartition, "soc_int_job_" + i));
		}
		
		Map<String, List<MetricsExporter>> metricExporterMap = new HashMap<String, List<MetricsExporter>>();
		
		
	    List<Future<List<MetricsExporter>>> list = new ArrayList<Future<List<MetricsExporter>>>();
	    
	    for (Callable<List<MetricsExporter>> job : jobs) {
			Future<List<MetricsExporter>> submit = executor.submit(job);
			list.add(submit);
		}

	    for (Future<List<MetricsExporter>> future : list) {
	    	try {
	    		List<MetricsExporter> metrics = future.get();
	    		
	    		if (metricExporterMap.containsKey(metrics.get(0).getAlgorithmName())) {
	    			List<MetricsExporter> previousMetrics = metricExporterMap.get(metrics.get(0).getAlgorithmName());
	    			
	    			for (int i = 0; i < previousMetrics.size(); i++) {
	    				previousMetrics.get(i).appendMetrics(metrics.get(i));
	    			}
	    			
	    			metricExporterMap.put(metrics.get(0).getAlgorithmName(), previousMetrics);
	    		} else {
	    			metricExporterMap.put(metrics.get(0).getAlgorithmName(), metrics);
	    		}
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	} catch (ExecutionException e) {
	    		e.printStackTrace();
	    	}
	    }
	    executor.shutdown();
	    
	    for (List<MetricsExporter> metrics : metricExporterMap.values()) {
	    	for (int n = 1; n <= 10; n++) {
				MetricsExporter mCalc = metrics.get(n-1);
				mCalc.exportCalculatedMetricsAverage(users.size());
			}
	    }
	    
	    long totalTime = System.nanoTime() - startTime;
	    System.out.println("Finished all threads in " + (totalTime / 1000000000) + " seconds");
	}

	private void initEvaluation() {
//		RecommenderEvaluator recommenderEval = new RecommenderEvaluator();
//		DataFetcher.getReviewingUsers();
		
		users = DataFetcher.getReviewingUsers();

//		List<String> socialUsers = recommenderEval.getAllSocialUsers();
//		
//		socialUsers.retainAll(allUsers);
//		allUsers.removeAll(socialUsers);
//
//		System.out.println(allUsers.size());
//		System.out.println(socialUsers.size());
//		
//		Collections.shuffle(socialUsers);
//		Collections.shuffle(allUsers);
//
//		int toRemove = (int) (socialUsers.size() * 0.0);
//		
//		users = socialUsers.subList(0, socialUsers.size() - toRemove);
//		users.addAll(allUsers.subList(0, toRemove));
		
		System.out.println("User Size " + users.size());
	}
	
}
