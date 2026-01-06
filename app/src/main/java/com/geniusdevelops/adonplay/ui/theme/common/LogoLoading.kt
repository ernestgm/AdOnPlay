/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geniusdevelops.adonplay.ui.theme.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.geniusdevelops.adonplay.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LogoLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .width(300.dp)
                    .padding(all = 20.dp),
                painter = painterResource(id = R.drawable.ic_adonplay),
                contentDescription = ""
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )
            Loading(
                text = "", modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}
