package net.coding.program.common.model.user;

import java.util.List;

/**
 * Created by anfs on 30/11/2016.
 */

public class ActiveModel {
    public List<ActivenessModel> daily_activeness;
    public long total;
    public long last_activity_at;
    public long total_with_seal_top_line;
    public String start_date;
    public String end_date;
    public ActiveDurationModel longest_active_duration;
    public ActiveDurationModel current_active_duration;
}
