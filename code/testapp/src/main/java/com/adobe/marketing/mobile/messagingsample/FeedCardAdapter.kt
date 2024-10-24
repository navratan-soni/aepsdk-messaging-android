/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.messagingsample

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adobe.marketing.mobile.MessagingEdgeEventType
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.messaging.Proposition
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.DataReader

class FeedCardAdapter(propositions: MutableList<Proposition>) :
    RecyclerView.Adapter<FeedCardAdapter.ViewHolder>() {
    private var propositions = mutableListOf<Proposition>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_feeditem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proposition = propositions[position]
        for (item in proposition.items) {
            val inboundContent = item.contentCardSchemaData
            val contentMap = inboundContent?.content as HashMap<String, Object>
            val contentCard = MyContentCard()
            contentCard.title =
                DataReader.getStringMap(contentMap, "title")["content"] ?: "defaultTitle"
            contentCard.body =
                DataReader.getStringMap(contentMap, "body")["content"] ?: "defaultBody"
            contentCard.imageUrl = DataReader.getStringMap(contentMap, "image")["url"]
            contentCard.imageUrl?.let {
                holder.feedItemImage.setImageBitmap(ImageDownloader.getImage(it))
            }
            holder.feedItemImage.refreshDrawableState()
            holder.feedItemTitle.text = contentCard.title
            holder.feedBody.text = contentCard.body
            item.track(MessagingEdgeEventType.DISPLAY)
            holder.itemView.setOnClickListener {
                item.track(MessagingEdgeEventType.INTERACT)
                val intent = Intent(
                    ServiceProvider.getInstance().appContextService.applicationContext,
                    SingleFeedActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                MobileCore.getApplication()?.startActivity(intent.apply {
                    putExtra("content", contentMap)
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return propositions.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val feedItemImage: ImageView
        val feedItemTitle: TextView
        val feedBody: TextView

        init {
            feedItemImage = itemView.findViewById(R.id.feedItemImage)
            feedItemTitle = itemView.findViewById(R.id.feedItemTitle)
            feedBody = itemView.findViewById(R.id.feedBody)
        }
    }

    init {
        this.propositions = propositions
    }
}

class MyContentCard {
    var title: String = ""
    var body: String = ""
    var imageUrl: String? = null
}