using System;
using System.Collections.Generic;
using System.Text;

namespace WSClientDB.dataClasses
{
    class DataOfMessage
    {
        public string dialog_id { get; set; }
        public string sender { get; set; }
        public string typeMsg { get; set; }
        public string textMsg { get; set; }
        public int timeCreated { get; set; }
    }
}
