package com.github.jenkins.lastchanges.api;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by rmpestano on 6/26/16.
 */
public class CommitInfo {

    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
    private static final String newLine = System.getProperty("line.separator");

    private String commitId;
    private String commitMessage;
    private String commiterName;
    private String commiterEmail;
    private String commitDate;

    private CommitInfo() {
    }

    public String getCommiterName() {
        return commiterName;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getCommiterEmail() {
        return commiterEmail;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().
                append("Commit: ").append(commitId).append(newLine).
                append("Author: "+commiterName).append(newLine).
                append("E-mail: ").append(commiterEmail).append(newLine).
                append("Date: ").append(commitDate).append(newLine).
                append("Message: ").append(commitMessage).append(newLine).append(newLine);

        return sb.toString();
    }


    public static class Builder {

        public static CommitInfo buildCommitInfo(Repository repository, ObjectId commitId) {
            RevWalk walk = new RevWalk(repository);

            try {
                ObjectId lastCommitId = repository.resolve(Constants.HEAD);
                RevWalk revWalk = new RevWalk(repository);
                RevCommit commit = revWalk.parseCommit(lastCommitId);
                CommitInfo commitInfo = new CommitInfo();
                PersonIdent committerIdent = commit.getCommitterIdent();
                Date commitDate = committerIdent.getWhen();
                commitInfo.commitId = commitId.getName();
                commitInfo.commitMessage = commit.getFullMessage();
                commitInfo.commiterName = committerIdent.getName();
                commitInfo.commiterEmail = committerIdent.getEmailAddress();
                TimeZone tz = committerIdent.getTimeZone() != null ? committerIdent.getTimeZone() : TimeZone.getDefault();
                dateFormat.setTimeZone(tz);
                commitInfo.commitDate = dateFormat.format(commitDate) + " " + tz.getDisplayName();
                return commitInfo;
            } catch (Exception e) {
                throw new RuntimeException("Could not get commit info for commit id: "+commitId,e);

            }
            finally {
                if(walk != null){
                    walk.dispose();
                    walk.close();
                }
            }

        }
    }

}