/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.dashboard.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;



@Comment("A download is the process of saving entities from the repositry to the users computer. ")
@Entity
public class DownloadEntityAge extends EntityBaseBean implements Serializable {
    
    @Comment("A download file age is associated to one download.")
    @JoinColumn(name = "DOWNLOAD_ID", nullable=false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Download download;
    
    
    @Comment("The age of files in the format of days.")
    private long age;
    
    @Comment("The amount of files.")
    private long amount;

    public Download getDownload() {
        return download;
    }

    public long getAge() {
        return age;
    }

    public long getAmount() {
        return amount;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public void setAmount(long number) {
        this.amount = number;
    }
    
    
    
}
