package com.example.goncalomatos.spotifysdktest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by goncalomatos on 10/01/16.
 */
public class Run {
    private Date startDate;
    private Date endDate;
    private List<RunStat> runStats;

    public Run() {
        setStartDate(new Date());
        runStats = new ArrayList<RunStat>();
    }

    public void addStat(RunStat stat) {
        runStats.add(stat);
    }

    public void finishRun(){
        setEndDate(new Date());
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<RunStat> getRunStats() {
        return runStats;
    }

    public void setRunStats(List<RunStat> runStats) {
        this.runStats = runStats;
    }
}
