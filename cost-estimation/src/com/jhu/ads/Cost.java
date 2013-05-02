package com.jhu.ads;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;

public class Cost {
    
    /* Common */
    public long commonUsersPerInstance;
    public long uncommonUsersPerInstance;
    public double commonUncommonRatio;
    public long maxConcurrentUsers;
    public double bandwidthPerUser; /* Mbps */
    
    /* Build your own */
    public long buildCostPerInstance; /* In dollars including electricity for UPGRADE_YEARS years */
    public double upgradeYears;
    public double costFor1Mbps; /* dollars per user per month */
    
    /* Rent */
    public long rentCostPerInstance; /* In dollars per year, for medium utilization */
    public double costPerGigaByteTransfer;
    
    /* Misc */
    public long scaleUsersBy;
    
    public ArrayList<Double> percentageList;
    
    public static final long MONTHS_IN_AN_YEAR = 12;
    public static final long DAYS_IN_MONTH = 30;
    
    private Properties props; 
    
    public Cost(String filePath) {
        try {
            FileInputStream fin  = new FileInputStream(filePath);
            props = new Properties();
            props.load(fin);
            fin.close();
            
            commonUsersPerInstance = getL("COMMON_USERS_PER_INSTANCE");
            uncommonUsersPerInstance = getL("UNCOMMON_USERS_PER_INSTANCE");
            commonUncommonRatio = getD("commonUncommonRatio");
            maxConcurrentUsers = getL("MAX_CONCURRENT_USERS");
            bandwidthPerUser = getD("BANDWIDTH_PER_USER"); /* Mbps */
            
            buildCostPerInstance = getL("BUILD_COST_PER_INSTANCE"); /* In dollars including electricity for UPGRADE_YEARS years */
            upgradeYears = getD("UPGRADE_YEARS");
            costFor1Mbps = getD("COST_FOR_1_Mbps"); /* dollars per user per month */
            
            rentCostPerInstance = getL("RENT_COST_PER_INSTANCE"); /* In dollars per year, for medium utilization */
            costPerGigaByteTransfer = getD("COST_PER_GIGABYTE_TRANSFER");
            
            scaleUsersBy = getL("SCALE_USERS_BY");
            
            percentageList = getAL("percentageList");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public ArrayList<Double> getAL(String keyPrefix) {
        Set<Object> keySet = props.keySet();
        ArrayList<Double> retList = new ArrayList<Double>();
        for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
            String propKey = (String) iterator.next();
            if(propKey.startsWith(keyPrefix)) {
                String idxStr = propKey.substring(propKey.lastIndexOf(".")+1);
                int idx  = Integer.parseInt(idxStr) - 1;
                while(idx >= retList.size()) {
                    retList.add(null);
                }
                retList.set(idx, Double.parseDouble(props.getProperty(propKey)));
            }
        }
        return retList;
    }

    public long getL(String key) {
        return Long.parseLong(props.getProperty(key).trim());
    }
    
    public Double getD(String key) {
        return Double.parseDouble(props.getProperty(key).trim());
    }
    
    public long computeBuildCost(long numUsers) {
        
        /* Infrastructure Cost */
        long commonMovieInstances = (long)((numUsers*commonUncommonRatio)/commonUsersPerInstance);
        long uncommonMovieInstances = (long)((numUsers*(1-commonUncommonRatio))/uncommonUsersPerInstance);
        long totalInstances = commonMovieInstances + uncommonMovieInstances;
        long infrastructureCost = totalInstances * buildCostPerInstance;
        long infrastructureCostPerYear = (long)(infrastructureCost/upgradeYears); /* Effective Per year cost */
        
        /* Network Cost */
        long networkCostPerYear = (long)(numUsers * costFor1Mbps * bandwidthPerUser * MONTHS_IN_AN_YEAR);
        System.out.println("infrastructureCostPerYear = " + infrastructureCostPerYear + " networkCostPerYear = " + networkCostPerYear);
        return (infrastructureCostPerYear + networkCostPerYear);   
    }
    
    public long computeRentCost(long numBuildUsers) {
        
        long numRentUsers = maxConcurrentUsers - numBuildUsers;
        
        /* Effective Infrastructure Cost */
        long commonMovieInstances = (long)((numRentUsers*commonUncommonRatio)/commonUsersPerInstance);
        long uncommonMovieInstances = (long)((numRentUsers*(1-commonUncommonRatio))/uncommonUsersPerInstance);
        long effInfrastructureCostPerYear = ((commonMovieInstances + uncommonMovieInstances) * rentCostPerInstance);
        
        /* Network Cost */
        double totalDataNeededPerScaleUsers = (double) ((24 * 3600 * bandwidthPerUser * scaleUsersBy)/(8*1024)); /* For one day */
        long moreUsers = numRentUsers;
        int startIndex = (int)numBuildUsers/1000000;
        int idx = startIndex;
        double totalDataPerDay = 0;
                
        while(moreUsers > 0) {
            totalDataPerDay += (totalDataNeededPerScaleUsers * percentageList.get(idx));
            moreUsers -= scaleUsersBy;
            if(moreUsers % 1000000 == 0) { /* TODO: Cannot scale if 1000000 is not a multiple of SCALE_USERS_BY */ 
                idx++;
            }
        }
        
        // System.out.println("totalDataNeededPerMonth= " + totalDataNeededPerMonth + " rentDataNeededPerMonth = " + rentDataNeededPerMonth);
        
        long costRentData = (long)(totalDataPerDay * DAYS_IN_MONTH * costPerGigaByteTransfer);
        long costRentDataPerYear = costRentData * MONTHS_IN_AN_YEAR;
        
        System.out.println("EffInfrastructureCostPerYear = " + effInfrastructureCostPerYear + " networkCostPerYear = " + costRentDataPerYear);
        return (effInfrastructureCostPerYear + costRentDataPerYear);
    }
    
    public static void main(String[] args) {
        Cost c = new Cost("C:\\JHU\\Sem4\\AdvDistributed\\proj\\cost-estimation\\src\\values.properties");
        
        CategoryTableXYDataset dataset = new CategoryTableXYDataset();
        
        for (long numUsers = 0; numUsers <= c.maxConcurrentUsers; numUsers+=c.scaleUsersBy) {
            System.out.println("[ BuildUsers: " + numUsers + ", RentUsers: " + (c.maxConcurrentUsers - numUsers) +"] ");
            long buildCost = c.computeBuildCost(numUsers);
            long rentCost = c.computeRentCost(numUsers);
            System.out.println("BuildCost = " + buildCost/1000000
                                        + " RentCost = " + rentCost/1000000 
                                        + " TotalCost = " + ( (double)buildCost/1000000 + (double)rentCost/1000000) );
            
            dataset.add(numUsers/c.scaleUsersBy, buildCost/1000000, "Build");
            dataset.add(numUsers/c.scaleUsersBy, rentCost/1000000, "Rent");
            
            System.out.println();
        }
    
    
        XYPlot plot = new XYPlot(dataset, new NumberAxis(
                "Users Served by Build Data Center(in "
                        + (c.scaleUsersBy == 1000000 ? "million": c.scaleUsersBy) + ")  " 
                        + "TotalUsers=" + c.maxConcurrentUsers / 1000000 + " million"),
                        new NumberAxis("Total Cost(in Million)"), new StackedXYBarRenderer());
        
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Cost Analysis");
        frame.setContentPane(chartPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
}
