using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class DownLoadAllMsg
    {
        public List<string> dialog_ids { get; set; }
        public string authorId { get; set; }
        public string token { get; set; }
    }
}
