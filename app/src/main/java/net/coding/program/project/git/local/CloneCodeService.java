package net.coding.program.project.git.local;

import android.app.IntentService;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import net.coding.program.common.Global;
import net.coding.program.common.event.EventDownloadError;
import net.coding.program.common.event.EventDownloadProgress;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.param.ProjectJumpParam;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.Serializable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CloneCodeService extends IntentService {

    private static final String TAG = "CloneCodeService";

    private static final String ACTION_GIT = "net.coding.program.project.git.local.action.git";

    private static final String EXTRA_PARAM1 = "net.coding.program.project.git.local.extra.PARAM1";

    public CloneCodeService() {
        super("CloneCodeService");
    }

    public static class Param implements Serializable {

        ProjectJumpParam parojectParm;
        String gitUrl = "https://git.coding.net/coding/test-point.git";

        String gk;
        String password;

        public Param(ProjectObject projectObject, String gk, String password) {
            parojectParm = new ProjectJumpParam(projectObject.owner_user_name, projectObject.name);
            gitUrl = projectObject.https_url;
            this.gk = gk;
            this.password = password;
        }

        String getUrl() {
            return gitUrl;
        }

        File getFile(ContextWrapper context) {
            return new File(context.getFilesDir(), String.format("%s/%s/%s", gk, parojectParm.user, parojectParm.project));
        }
    }

    public static void startActionGit(Context context, Param param) {
        Intent intent = new Intent(context, CloneCodeService.class);
        intent.setAction(ACTION_GIT);
        intent.putExtra(EXTRA_PARAM1, param);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GIT.equals(action)) {
                final Param param = (Param) intent.getSerializableExtra(EXTRA_PARAM1);
                handleActionGit(param);
            }
        }
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    private void handleActionGit(Param param) {
        try {
            File file = param.getFile(this);
            Repository repo = new FileRepository(file);
            if (file.exists() && file.isDirectory()) {
                if (repo.isBare()) {
                    deleteDirectory(file);
                    cloneSource(param);
                } else {
                    Git git = new Git(repo);
                    git.pull().setProgressMonitor(new TextProgressMonitor()).call();
                }
            } else {
                deleteDirectory(file);
                cloneSource(param);
            }
        } catch (Exception e) {
            Global.errorLog(e);
            EventBus.getDefault().post(new EventDownloadError(e.getMessage()));
        }
    }

    private void cloneSource(Param param) throws Exception {
        String gitUrl = param.getUrl();
        File file = param.getFile(this);
        Git.cloneRepository()
                .setURI(gitUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(param.gk, param.password))
                .setDirectory(file)
                .setProgressMonitor(new TextProgressMonitor())
                .call();
        Log.d(TAG, "finish ");
    }

    static class TextProgressMonitor extends BatchingProgressMonitor {

        private boolean write;

        TextProgressMonitor() {
            this.write = true;
        }

        @Override
        protected void onUpdate(String taskName, int workCurr) {
            StringBuilder s = new StringBuilder();
            format(s, taskName, workCurr);
            send(s);
        }

        @Override
        protected void onEndTask(String taskName, int workCurr) {
            StringBuilder s = new StringBuilder();
            format(s, taskName, workCurr);
//            s.append("\n"); //$NON-NLS-1$
            send(s);
        }

        private void format(StringBuilder s, String taskName, int workCurr) {
            s.append("\r"); //$NON-NLS-1$
            s.append(taskName);
            s.append(": "); //$NON-NLS-1$
            while (s.length() < 25)
                s.append(' ');
            s.append(workCurr);
        }

        @Override
        protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
            StringBuilder s = new StringBuilder();
            format(s, taskName, cmp, totalWork, pcnt);
            send(s);
        }

        @Override
        protected void onEndTask(String taskName, int cmp, int totalWork, int pcnt) {
            StringBuilder s = new StringBuilder();
            format(s, taskName, cmp, totalWork, pcnt);
//            s.append("\n"); //$NON-NLS-1$
            send(s);
        }

        private void format(StringBuilder s, String taskName, int cmp,
                            int totalWork, int pcnt) {
            s.append("\r"); //$NON-NLS-1$
            s.append(taskName);
            s.append(": "); //$NON-NLS-1$
            while (s.length() < 25)
                s.append(' ');

            String endStr = String.valueOf(totalWork);
            String curStr = String.valueOf(cmp);
            while (curStr.length() < endStr.length())
                curStr = " " + curStr; //$NON-NLS-1$
            if (pcnt < 100)
                s.append(' ');
            if (pcnt < 10)
                s.append(' ');
            s.append(pcnt);
            s.append("% ("); //$NON-NLS-1$
            s.append(curStr);
            s.append("/"); //$NON-NLS-1$
            s.append(endStr);
            s.append(")"); //$NON-NLS-1$
        }

        private void send(StringBuilder s) {
            if (write) {
                try {
//                    Logger.d(TAG, s);
                    Log.d(TAG, "jjjjjjjjj " + s);
                    EventBus.getDefault().post(new EventDownloadProgress(s.toString()));
                } catch (Exception err) {
                    write = false;
                }
            }
        }
    }

}
