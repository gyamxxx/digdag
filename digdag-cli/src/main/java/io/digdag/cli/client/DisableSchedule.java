package io.digdag.cli.client;

import com.google.common.base.Optional;
import io.digdag.cli.SystemExitException;
import io.digdag.client.DigdagClient;
import io.digdag.client.api.RestProject;
import io.digdag.client.api.RestSchedule;

import java.io.IOException;
import java.util.List;

import static io.digdag.cli.SystemExitException.systemExit;

public class DisableSchedule
        extends ClientCommand
{
    @Override
    public SystemExitException usage(String error)
    {
        err.println("Usage: " + programName + " disable <id> | <project-name> [name]");
        showCommonOptions();
        err.println("");
        err.println("  Examples:");
        err.println("    $ " + programName + " disable 17           # Disable workflow schedule 17");
        err.println("    $ " + programName + " disable myproj       # Disable all workflow schedules in the project 'myproj'");
        err.println("    $ " + programName + " disable myproj mywf  # Disable the schedule of the workflow 'mywf'");
        err.println("");
        return systemExit(error);
    }

    @Override
    public void mainWithClientException()
            throws Exception
    {
        if (args.size() == 1) {
            // Schedule id?
            Integer scheduleId = tryParseInt(args.get(0));
            if (scheduleId != null) {
                disableSchedule(scheduleId);
            }
            else {
                // Project name?
                disableProjectSchedules(args.get(0));
            }
        }
        else if (args.size() == 2) {
            // Single workflow
            disableWorkflowSchedule(args.get(0), args.get(1));
        }
        else {
            throw usage(null);
        }
    }

    private static Integer tryParseInt(String s)
    {
        try {
            return Integer.parseUnsignedInt(s);
        }
        catch (NumberFormatException ignore) {
            return null;
        }
    }

    private void disableWorkflowSchedule(String projectName, String workflowName)
            throws IOException, SystemExitException
    {
        DigdagClient client = buildClient();
        RestProject project = client.getProject(projectName);
        RestSchedule schedule = client.getSchedule(project.getId(), workflowName);
        client.disableSchedule(schedule.getId());
        ln("Disabled schedule id: %d", schedule.getId());
    }

    private void disableProjectSchedules(String projectName)
            throws IOException, SystemExitException
    {
        DigdagClient client = buildClient();
        RestProject project = client.getProject(projectName);
        List<RestSchedule> schedules;
        Optional<Integer> lastId = Optional.absent();
        while (true) {
            schedules = client.getSchedules(project.getId(), lastId);
            if (schedules.isEmpty()) {
                return;
            }
            for (RestSchedule schedule : schedules) {
                client.disableSchedule(schedule.getId());
                ln("Disabled schedule id: %d", schedule.getId());
            }
            lastId = Optional.of(schedules.get(schedules.size() - 1).getId());
        }
    }

    private void disableSchedule(int scheduleId)
            throws IOException, SystemExitException
    {
        DigdagClient client = buildClient();
        client.disableSchedule(scheduleId);
        ln("Disabled schedule id: %d", scheduleId);
    }
}
