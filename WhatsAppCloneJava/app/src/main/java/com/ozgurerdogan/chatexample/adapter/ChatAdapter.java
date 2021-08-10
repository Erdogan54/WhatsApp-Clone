package com.ozgurerdogan.chatexample.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ozgurerdogan.chatexample.R;
import com.ozgurerdogan.chatexample.databinding.SampleRecieverBinding;
import com.ozgurerdogan.chatexample.databinding.SampleSenderBinding;
import com.ozgurerdogan.chatexample.models.MessageModel;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModelArrayList;
    Context context;

    int SENDER_VIEW_TYPE=1;
    int RECEIVER_VIEW_TYPE=2;

    public ChatAdapter(ArrayList<MessageModel> messageModelArrayList, Context context) {
        this.messageModelArrayList = messageModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==SENDER_VIEW_TYPE){
            View view=LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            return new SenderViewHolder(view);

        }else{
            View view=LayoutInflater.from(context).inflate(R.layout.sample_reciever,parent,false);
            return new RecieverViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel=messageModelArrayList.get(position);

        if(holder.getClass()==SenderViewHolder.class){
            ((SenderViewHolder) holder).senderText.setText(messageModel.getMessage());
        }else if(holder.getClass()==RecieverViewHolder.class){
            ((RecieverViewHolder) holder).recieverText.setText(messageModel.getMessage());
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String uid=messageModel.getSendUid();
                String recid=messageModel.getRecUid();
                String messageid= messageModel.getTimestamp().toString();

                AlertDialog.Builder alert=new AlertDialog.Builder(context);
                alert.setTitle("Delete");
                alert.setMessage("Do you can already delete message?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(uid+recid)
                                .child(messageid)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();

                return true;

            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if(messageModelArrayList.get(position).getSendUid().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }else{
            return RECEIVER_VIEW_TYPE;
        }

    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }


    public class RecieverViewHolder extends RecyclerView.ViewHolder {


        TextView recieverText,recieverTime;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverText=itemView.findViewById(R.id.recieverText);
            recieverTime=itemView.findViewById(R.id.recieverTime);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderText,senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderText=itemView.findViewById(R.id.senderText);
            senderTime=itemView.findViewById(R.id.senderTime);
        }
    }


}
